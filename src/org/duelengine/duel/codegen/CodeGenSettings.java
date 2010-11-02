package org.duelengine.duel.codegen;

public class CodeGenSettings {

	private String indent = "\t";
	private String newline = "\n";
	private boolean encodeNonASCII = true;

	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String value) {
		this.indent = value;
	}

	public String getNewline() {
		return this.newline;
	}

	public void setNewline(String value) {
		this.newline = value;
	}

	public boolean getEncodeNonASCII() {
		return this.encodeNonASCII;
	}

	public void setEncodeNonASCII(boolean value) {
		this.encodeNonASCII = value;
	}
}
