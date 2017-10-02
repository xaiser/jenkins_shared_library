import groovy.transform.Field
import utility.*

@Field def utility = new utility()

def send_cmake(cmake_command) {
	if ( isUni() ) {
		/* current node is Unix-base */
		sh "cmake ${cmake_command}"
		//echo "${cmake_command}"
	}
	else {
		bat "cmake ${cmake_command}"
		//echo "${cmake_command}"
	}
}

def cmake_build(args) {
	assert true == args.containsKey("project_name")
	assert true == args.containsKey("build_type")

	String cmake_param = "--build . --target ${args.project_name} --config ${args.build_type}"

	send_cmake(cmake_param)
}

def cmake_config(args) {
	assert true == args.containsKey("qt_project")
	assert true == args.containsKey("build_type")
	assert true == args.containsKey("lib_path")
	assert true == args.containsKey("arch")
	assert true == args.containsKey("cmake_generator")
	assert true == args.containsKey("CMakeListPath")

	String cmake_param = "-Dqt_project=${args.qt_project} -DCMAKE_BUILD_TYPE=${args.build_type} -DLib_path=${args.lib_path} -Darch=${args.arch} -G \"${args.cmake_generator}\" ${args.CMakeListPath}"
		
	if ( args.containsKey("is_android") && "ON" == args.is_android ) {
		cmake_param = "-Dis_android=ON " + cmake_param
	}

	if( args.containsKey("android_base") )
	{
		cmake_param = "-Dandroid_base=${args.android_base} " + cmake_param
	}

	if( args.containsKey("test_param") )
	{
		cmake_param = "${args.test_param} " + cmake_param
	}

	send_cmake(cmake_param)
}

def cmake_ctest(args) {
	assert true == args.containsKey("build_type")

	String ctest_param = "-C ${args.build_type} -V"

	if ( utility.isWindows() ) {
		bat "ctest ${ctest_param}"
		//echo "windows"
		//echo "${ctest_param}"
	}
	else {
		assert true == args.containsKey("sudo_env")
		sh "sudo -E ${sudo_env} -S ctest ${ctest_param}"
		//echo "linux"
		//echo "sudo -E ${sudo_env} -S ${ctest_param}"
	}
}

def cmake_install(args) {
	assert true == args.containsKey("project_name")
	assert true == args.containsKey("build_type")

	String cmake_param = "--build . --target ${args.project_name}_install --config ${args.build_type}"

	send_cmake(cmake_param)
}

def call(args) {
	assert true == args.containsKey("act")

	if ( "config" == args.act ) {
		cmake_config(args)
	}
	else if ( "build" == args.act ) {
		cmake_build(args)
	}
	else if ( "install" == args.act ) {
		cmake_install(args)
	}
	else if ( "ctest" == args.act ) {
		cmake_ctest(args)
	}
	else {
		error("unknow cmake act ${args.act}")
	}
}
