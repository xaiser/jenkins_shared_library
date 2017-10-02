import groovy.transform.Field
import utility.*
import hudson.FilePath

@Field def utility = new utility()
@Field def android_utility = new android_sdk_utility()

def check_param(config) {
	assert config.containsKey("solution_name")
	assert config.containsKey("project_name")
	assert config.containsKey("compiler")
	assert config.containsKey("arch")
	assert config.containsKey("skip_composition")
	assert config.containsKey("git_branch")
	assert config.containsKey("lib_type")
	assert config.containsKey("lib_path")
	assert config.containsKey("build_type")
	assert config.containsKey("git_repo_url")
	assert config.containsKey("build_node")

	if ( config.containsKey("is_android") ) {
		assert config.containsKey("ANDROID_NDK_ROOT")
		assert config.containsKey("ANDROID_NDK_HOST")
		assert config.containsKey("ANDROID_SDK_ROOT")
	}
}

def clean_workspace(compiler, arch, build_type) {
	def sub_workspace = "${compiler}_${arch}_${build_type}"
	stage("Clean workspace ${sub_workspace}") {
		def workspace_folder = new File("${WORKSPACE}/${sub_workspace}")
		if ( workspace_folder.exists() )
		{
			def is_delete = workspace_folder.deleteDir()
			assert is_delete
		}
		def is_mkdir = workspace_folder.mkdirs()
		assert is_mkdir
	}
}

def build(sub_workspace, compiler, arch, build_type, config) {
	String lib_type = config.lib_type
	String pro_name = config.project_name
	String lib_path = config.lib_path
	boolean is_android = config.containsKey("is_android") && "ON" == config.is_android

	if ( true == is_android ) {
		env.ANDROID_NDK_ROOT = config.ANDROID_NDK_ROOT
		env.ANDROID_NDK_HOST = config.ANDROID_NDK_HOST
	}
	dir("${sub_workspace}") {
		if ( true == is_android) {
			android_utility.start_AVD(config.ANDROID_SDK_ROOT)
		}
		stage("Clone for ${compiler} ${arch} ${build_type}") {
			git url:"${config.git_repo_url}/${config.solution_name}", branch:"${config.git_branch}", credentialsId: "add credential ID"
		}
		stage("build ${compiler} ${arch} ${build_type}") {
			cg = utility.get_cmake_generator(compiler, arch)
			def full_proj_name = "${lib_type}${pro_name}Test"
			String cmakelists_path = "auto" == config.cmakelists_path ? "test/${full_proj_name}" : "${config.cmakelists_path}"

			def test_param = "-DGTEST_XML_OUTPUT=${compiler}_${arch}_${build_type}.xml " + "${config.test_param}"

			if ( true == is_android ) {
				def android_base = utility.get_android_base(config.ANDROID_SDK_ROOT)
				cmake act: "config", qt_project: "${full_proj_name}.pro", build_type: "${build_type}", \
					lib_path: "${lib_path}", arch: "${arch}", cmake_generator: "${cg}", \
					test_param: "${test_param}", \
					is_android: "ON", android_base: "${android_base}", \
					CMakeListPath: "${cmakelists_path}"
			} else {
				cmake act: "config", qt_project: "${full_proj_name}.pro", build_type: "${build_type}", \
					lib_path: "${lib_path}", arch: "${arch}", cmake_generator: "${cg}", \
					test_param: "${test_param}", \
					CMakeListPath: "${cmakelists_path}"
			}

			cmake act: "build", project_name: "${full_proj_name}", build_type: "${build_type}"
		}
	}
}

def test_and_report(sub_workspace, compiler, arch, build_type, config) {
	String lib_type = config.lib_type
	String pro_name = config.project_name
	String lib_path = config.lib_path
	dir("${sub_workspace}") {
		def qt_dir = utility.get_qt_dir_by(compiler, arch)
		stage("test ${compiler} ${arch} ${build_type}") {
			def err = null
			boolean is_android = config.containsKey("is_android") && "ON" == config.is_android
			try {
				if ( utility.isWindows() ) {
					if ( true == is_android) {
						cmake act: "ctest", build_type: "${build_type}"
						android_utility.pull_xml(config.ANDROID_SDK_ROOT, "/data/${lib_type}${pro_name}Test/${compiler}_${arch}_${build_type}.xml")
					} else {
						/* run with qt directory */
						withEnv(["PATH=${qt_dir}\\bin;${env.PATH}"]) {
							cmake act: "ctest", build_type: "${build_type}"
						}
					}
				} else {
					String sudo_env = "LD_LIBRARY_PATH=\"${qt_dir}/bin:.:$LD_LIBRARY_PATH\""
					cmake act: "ctest", build_type: "${build_type}", sudo_env: "${sudo_env}"
				}
			} catch(caught_error) {
				err = caught_error
			} finally {
				junit '**/*.xml'
				if(err) {
					throw err
				}
			}
		}
	}
}

def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def err = null
	try {
		check_param(config)

		def param_parser = new ParameterParser()
		param_parser.parse(config)
		def param = param_parser.get_param()

		/* 
		 * We can not use the for-in loop becasue of a known bug.
		 * https://issues.jenkins-ci.org/browse/JENKINS-34645.
		 * And the workaround is to use tranditional for loop.
		 */

		for ( int btIdx = 0; btIdx < param.build_type.size(); btIdx++ ) {
			for ( int cIdx = 0; cIdx < param.compiler.size(); cIdx++ ) {
				for ( int aIdx = 0; aIdx < param.arch.size(); aIdx++ ) {
					def build_type = param.build_type[btIdx]
					def compiler = param.compiler[cIdx]
					def arch = param.arch[aIdx]
					if ( utility.isSkip(compiler, arch, param.skip_composition) ) {
						println "skip ${compiler}-${arch}"
						continue
					}
					node_name = "auto" == param.build_node ? utility.get_node_name(compiler, arch) : param.build_node
					def sub_workspace = "${compiler}_${arch}_${build_type}"
					node("${node_name}") {
						clean_workspace(compiler, arch, build_type)
						build(sub_workspace, compiler, arch, build_type, param)
						test_and_report(sub_workspace, compiler, arch, build_type, param)
					}
				}
			}
		}
	} catch (catch_err) {
		println catch_err
		err = catch_err
		currentBuild.result = 'FAILURE'
	} finally {
		boolean is_android = param.containsKey("is_android") && "ON" == param.is_android
		if ( true == is_android) {
			android_utility.shut_down_AVD(param.ANDROID_SDK_ROOT)
		}
		if(err) {
			throw err
		}
	}
}
