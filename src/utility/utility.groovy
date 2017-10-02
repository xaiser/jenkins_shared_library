package utility

boolean isWindows()
{
	if ( System.properties['os.name'].toLowerCase().contains('windows') ) {
		return true;
	}
	else {
		return false;
	}
}

String get_android_base(sdk_root) {
	String[] _folders = sdk_root.split(/['\/'|'\\']/)
	String[] _folders_removed_last = _folders[0 .. _folders.size() - 2]
	String android_base = _folders_removed_last.join("\\")

	return android_base
}

String get_cmake_generator(compiler, arch) {
	List windows_tool_chain = ["Visual Studio 14 2015", "Visual Studio 14 2014 x64", "Visual Studio 10 2010", "Visual Studio 10 2010 x64", "Visual Studio 9 2008"]
	List linux_tool_chain = ["Unix Makefiles"]
	String generator = ""

	if ( compiler.contains("msvc") ) {
		generator = "Visual Studio "
		if ( compiler.contains("2008") ) {
			generator += "9 2008"
		} else if ( compiler.contains("2010") ) {
			generator += "10 2010"
		} else if ( compiler.contains("2015") ) {
			generator += "14 2015"
		}

		if ( "x64" == arch ) {
			generator += " Win64"
		}
	} else if ( compiler.contains("g++") ) {
		generator = "Unix Makefiles"
	}
	return generator
}

boolean isSkip(compiler, arch, skip_composition) {
	for ( sc in skip_composition ) {
		if ( sc == "${compiler}-${arch}" )
		{
			return true;
		}
	}
	return false;
}

String get_node_name(compiler, arch) {
	if ( compiler.contains("msvc") ) {
		return "master"
	} else if (compiler.contains("g++") ) {
		if ( "x86" == arch ) {
			return "linux_x86"
		} else {
			return "linux_x64"
		}
	}
	error("Can not generate node name by ${compiler} ${arch}")
}

String get_qt_dir_by(compiler, arch) {
	def envKey
	if ( compiler.contains("msvc") ) {
		envKey = "win32_" + "${compiler}_" + "${arch}_" + "qt_dir"
	} else if (compiler.contains("g++") ) {
		envKey = "linux_gcc_qt_dir"
	}
	envKey = envKey.toUpperCase()

	return env."${envKey}"
}
