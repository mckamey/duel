package org.duelengine.duel.codegen;

/**
 * Settings which affect generated code
 */
public class CodeGenSettings {

	private String namePrefix;
	private String indent = "\t";
	private String newline = "\n";
	private boolean convertLineEndings;
	private boolean normalizeWhitespace;
	private boolean encodeNonASCII = true;

	public void setNamePrefix(String value) {
		this.namePrefix = (value == null) ? null : value.trim();
	}

	public String getNamePrefix() {
		return this.namePrefix;
	}

	public boolean hasNamePrefix() {
		return (this.namePrefix != null) && (this.namePrefix.length() > 0);
	}

	public String getFullName(String name) {
		if ((this.namePrefix == null) || (this.namePrefix.length() < 1)) {
			return name;
		}

		if (name != null) {
			name = name.trim();
		}

		return this.namePrefix+'.'+name;
	}

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

	/**
	 * Gets if all whitespace literals should be normalized (replaced by single space)
	 * @return
	 */
	public boolean getNormalizeWhitespace() {
		return this.normalizeWhitespace;
	}

	/**
	 * Sets if all whitespace literals should be normalized (replaced by single space)
	 * @param value
	 */
	public void setNormalizeWhitespace(boolean value) {
		this.normalizeWhitespace = value;
	}
}
