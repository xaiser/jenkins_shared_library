package utility

def start_AVD(sdk_root) {
	bat "${sdk_root}\\tools\\emulator.exe -avd TestDevice -no-skin -no-audio -no-window"
	bat "${sdk_root}\\platform-tools\\adb.exe devices"
	bat "${sdk_root}\\platform-tools\\adb.exe kill-server"
	bat "${sdk_root}\\platform-tools\\adb.exe start-server"
	bat "${sdk_root}\\platform-tools\\adb.exe devices"

	/*
	echo "${sdk_root}\\tools\\emulator.exe -avd TestDevice -no-skin -no-audio -no-window"
	echo "${sdk_root}\\platform-tools\\adb.exe devices"
	echo "${sdk_root}\\platform-tools\\adb.exe kill-server"
	echo "${sdk_root}\\platform-tools\\adb.exe start-server"
	echo "${sdk_root}\\platform-tools\\adb.exe devices"
	*/
}

def pull_xml(sdk_root, xml) {
	bat "${sdk_root}\\platform-tools\\adb.exe pull ${xml} ."
	//echo "${sdk_root}\\platform-tools\\adb.exe pull ${xml} ."
}

def shut_down_AVD(sdk_root) {
	bat "${sdk_root}\\platform-tools\\adb.exe -s emulator-5554 emu kill"
	//echo "${sdk_root}\\platform-tools\\adb.exe -s emulator-5554 emu kill"
}
