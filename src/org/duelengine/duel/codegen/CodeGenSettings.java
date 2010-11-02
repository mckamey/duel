package org.duelengine.duel.codegen;

public class CodeGenSettings {

	private String indent = "\t";
	private String newline = "\n";

	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public String getNewline() {
		return this.newline;
	}

	public void setNewline(String newline) {
		this.newline = newline;
	}
}
