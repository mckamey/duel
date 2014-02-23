package org.duelengine.duel;

public class FormatPrefs {

	public static final String UTF8_ENCODING = "UTF-8";
	public static final String NEWLINE = "\n";
	public static final String INDENT = "\t";

	private String encoding = UTF8_ENCODING;
	private String newline = "";
	private String indent = "";
	private boolean encodeNonASCII;
	private boolean scriptTypeAttr;

	public String getEncoding() {
		return encoding;
	}

	public FormatPrefs setEncoding(String value) {
		if (value == null || value.isEmpty()) {
			// ensure non-empty
			encoding = UTF8_ENCODING;

		} else {
			encoding = value;
		}

		return this;
	}

	public String getNewline() {
		return newline;
	}

	public FormatPrefs setNewline(String value) {
		// ensure non-null
		newline = (value == null) ? "" : value;

		return this;
	}

	public String getIndent() {
		return indent;
	}

	public FormatPrefs setIndent(String value) {
		// ensure non-null
		indent = (value == null) ? "" : value;

		return this;
	}

	public boolean getEncodeNonASCII() {
		return encodeNonASCII;
	}

	public FormatPrefs setEncodeNonASCII(boolean value) {
		encodeNonASCII = value;

		return this;
	}

	public boolean getScriptTypeAttr() {
		return scriptTypeAttr;
	}

	public FormatPrefs setScriptTypeAttr(boolean value) {
		scriptTypeAttr = value;

		return this;
	}
}