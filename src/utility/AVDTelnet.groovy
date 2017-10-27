package utility

import org.apache.commons.net.telnet.EchoOptionHandler
import org.apache.commons.net.telnet.SuppressGAOptionHandler
import org.apache.commons.net.telnet.TelnetClient
import org.apache.commons.net.telnet.TerminalTypeOptionHandler
import java.io.InputStream;
import java.io.PrintStream;

class AVDTelnet implements Serializable {
	InputStream tin;
	PrintStream out;
	TelnetClient telnet;

	public AVDTelnet(String host, int port){
		telnet = new TelnetClient();

		// Connect to the specified server
		telnet.connect(host, port);

		// Get input and output stream references
		tin = telnet.getInputStream();
		out = new PrintStream(telnet.getOutputStream());
	}

	public String readUntil(String pattern) {
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			char ch = (char) tin.read();
			while (true) {
				print(ch);
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				ch = (char) tin.read();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "read until fail";
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String sendCommand(String command) {
		try {
			write(command);
			return readUntil("OK ");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "send fail";
	}

	public boolean killAVD() {
		try {
			write("kill");
			String result = readUntil("bye bye");

			return "read until fail" != result;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
