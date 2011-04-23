package org.duelengine.duel;

public class FormatPrefs {

	public static final String UTF8_ENCODING = "UTF-8";
	public static final String NEWLINE = "\n";
	public static final String INDENT = "\t";

	private String encoding = UTF8_ENCODING;
	private String newline = "";
	private String indent = "";
	private boolean encodeNonASCII;

	public String getEncoding() {
		return this.encoding;
	}

	public FormatPrefs setEncoding(String value) {
		if (value == null || value.isEmpty()) {
			// ensure non-empty
			this.encoding = UTF8_ENCODING;
		} else {
			this.encoding = value;
		}

		return this;
	}

	public String getNewline() {
		return this.newline;
	}

	public FormatPrefs setNewline(String value) {
		if (value == null) {
			// ensure non-null
			this.newline = "";
		} else {
			this.newline = value;
		}

		return this;
	}

	public String getIndent() {
		return this.indent;
	}

	public FormatPrefs setIndent(String value) {
		if (value == null) {
			// ensure non-null
			this.indent = "";
		} else {
			this.indent = value;
		}

		return this;
	}

	public boolean getEncodeNonASCII() {
		return this.encodeNonASCII;
	}

	public FormatPrefs setEncodeNonASCII(boolean value) {
		this.encodeNonASCII = value;

		return this;
	}
}