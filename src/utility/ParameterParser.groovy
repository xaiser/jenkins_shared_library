package utility

class ParameterParser implements Serializable {
	def param

	ParameterParser() {
		param = [:]
	}

	def parse(param_from_user) {
		for ( p in param_from_user ) {
			if( is_string_array(p.value) ) {
				param[p.key] = parse_string_array(p.value)
			} else {
				param[p.key] = p.value
			}
		}
	}

	def get_param(name) {
		return param.get(name)
	}

	def get_param() {
		return param
	}

	private boolean is_string_array(string) {
		return string =~ /^\[.+\]$/
	}

	def parse_string_array(string_array) {
		String[] sa;
		def match = string_array =~ /^\[(.+)\]$/
		if ( match ) {
			assert match.size() > 0
			sa = match[0][1].replaceAll('"', "").replaceAll("\\s", "").split(",")
		} else {
			assert false
		}
		return sa
	}
}

