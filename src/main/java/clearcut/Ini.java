package clearcut;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Ini extends HashMap<String, Object> {
	private static final String INI = "app.ini";
	private static String APP = INI;
	public static final String NO_SECTION = "_NO_SECTION_";
	private String path;

	private Ini() {
		super();
	}

	public Ini(String path) throws IniException {
		this();
		try {
			load(path);
			this.path = path;
		} catch (IOException x) {
			throw new IniException("Issue with " + path, x);
		}
	}

	public String path() {
		if (this.path == null)
			return null;
		if (this.path.endsWith(Ini.APP))
			return this.path.substring(0,
					this.path.length() - (1 + Ini.APP.length()));
		return this.path;
	}

	public static void app(String name) {
		Ini.APP = name + "-" + Ini.INI;
	}

	public static String app() {
		return Ini.APP;
	}

	/** This is mostly Sun's code from java.util.Properties */
	private synchronized void load(String path) throws IOException {
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(new File(path));
			char[] convtBuf = new char[1024];
			LineReader lr = new LineReader(inStream);
			int limit;
			int keyLen;
			int valueStart;
			char c;
			boolean hasSep;
			boolean precedingBackslash;
			String section = NO_SECTION; // Before reaching a 'section' in the
											// .ini file (eg. [database]),
			String lastSection = null; // we are in a section called
										// _NO_SECTION_
			List<String[]> list = new ArrayList<String[]>();
			while ((limit = lr.readLine()) >= 0) {
				c = 0;
				keyLen = 0;
				valueStart = limit;
				hasSep = false;
				precedingBackslash = false;
				while (keyLen < limit) {
					c = lr.lineBuf[keyLen];
					if ((c == '=') && !precedingBackslash) {
						valueStart = keyLen + 1;
						hasSep = true;
						break;
					} else if ((c == ' ' || c == '\t' || c == '\f')
							&& !precedingBackslash) {
						valueStart = keyLen + 1;
						break;
					}
					if (c == '\\')
						precedingBackslash = !precedingBackslash;
					else
						precedingBackslash = false;
					keyLen++;
				}
				while (valueStart < limit) {
					c = lr.lineBuf[valueStart];
					if (c != ' ' && c != '\t' && c != '\f')
						if (!hasSep && (c == '='))
							hasSep = true;
						else
							break;
					valueStart++;
				}
				String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
				key = key.trim();
				String value = "";
				// Windows 3.x and Bash shell style comments
				if (!key.startsWith(";") && !key.startsWith("#")) {
					if ((!hasSep) && key.startsWith("[") && key.endsWith("]")) {
						key = key.substring(1, key.length() - 1);
						key = key.substring(0, key.length());
						section = key;
					} else {
						value = loadConvert(lr.lineBuf, valueStart, limit
								- valueStart, convtBuf);
						if (!section.equals(lastSection))
							list = new ArrayList<String[]>();
						String[] entry = new String[2];
						entry[0] = key.trim();
						entry[1] = value.trim();
						list.add(entry);
						put(section, list);
						lastSection = section;
					}
				}
				// System.out.println(
				// "section:"+section+" key:"+key+" value:"+value+
				// " list size:"+list.size() );
			}
		} finally {
			try {
				inStream.close();
			} catch (Exception x) {
			}
		}
	}

	/** From java.util.Properties */
	class LineReader {
		public LineReader(InputStream inStream) {
			this.inStream = inStream;
		}

		byte[] inBuf = new byte[8192];
		char[] lineBuf = new char[1024];
		int inLimit = 0;
		int inOff = 0;
		InputStream inStream;

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = inStream.read(inBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				// The line below is equivalent to calling a
				// ISO8859-1 decoder.
				c = (char) (0xff & inBuf[inOff++]);
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = inStream.read(inBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}

	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = (char) aChar;
			}
		}
		return new String(out, 0, outLen);
	}

}