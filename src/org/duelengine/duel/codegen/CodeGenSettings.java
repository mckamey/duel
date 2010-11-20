package org.duelengine.duel.codegen;

/**
 * Settings which affect generated code
 */
public class CodeGenSettings {

	private String indent = "\t";
	private String newline = "\n";
	private boolean convertLineEndings = false;
	private boolean encodeNonASCII = true;

	/**
	 * Gets the string used for source indentation
	 * @return
	 */
	public String getIndent() {
		return this.indent;
	}

	/**
	 * Sets the string used for source indentation
	 * @param value
	 */
	public void setIndent(String value) {
		this.indent = (value != null) ? value : "";
	}

	/**
	 * Gets the string used for line endings
	 * @return
	 */
	public String getNewline() {
		return this.newline;
	}

	/**
	 * Sets the string used for line endings
	 * @param value
	 */
	public void setNewline(String value) {
		this.newline = (value != null) ? value : "";
	}

	/**
	 * Gets if line endings and tabs from original document
	 * should be normalized to their settings values
	 * @return
	 */
	public boolean getConvertLineEndings() {
		return this.convertLineEndings;
	}

	/**
	 * Sets if line endings and tabs from original document
	 * should be normalized to their settings values
	 * @param value
	 */
	public void setConvertLineEndings(boolean value) {
		this.convertLineEndings = value;
	}

	/**
	 * Gets if characters above ASCII should always be encoded in HTML
	 * @return
	 */
	public boolean getEncodeNonASCII() {
		return this.encodeNonASCII;
	}

	/**
	 * Sets if characters above ASCII should always be encoded in HTML
	 * @param value
	 */
	public void setEncodeNonASCII(boolean value) {
		this.encodeNonASCII = value;
	}
}
