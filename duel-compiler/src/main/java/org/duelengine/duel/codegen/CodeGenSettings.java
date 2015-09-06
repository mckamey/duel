package org.duelengine.duel.codegen;

import java.util.Locale;

/**
 * Settings which affect generated code
 */
public class CodeGenSettings {
	private static final char NAMESPACE_DELIM = '.';
	private static final char DIR_DELIM = '/';

	private String clientPrefix;
	private String serverPrefix;
	private String indent = "\t";
	private String newline = "\n";
	private boolean convertLineEndings;
	private boolean normalizeWhitespace;
	private boolean xhtmlStyle;
	private boolean encodeNonASCII = true;
	private boolean scriptTypeAttr;
	private boolean lowercaseClientPaths = true;

	public void setClientNamePrefix(String value) {
		clientPrefix = (value == null) ? null : value.trim();
	}

	public String getClientNamePrefix() {
		return clientPrefix;
	}

	public boolean hasClientNamePrefix() {
		return (clientPrefix != null) && !clientPrefix.isEmpty();
	}

	public void setServerNamePrefix(String value) {
		serverPrefix = value;
	}

	public String getServerNamePrefix() {
		return serverPrefix;
	}

	public boolean hasServerNamePrefix() {
		return (serverPrefix != null) && !serverPrefix.isEmpty();
	}

	/**
	 * Gets the string used for source indentation
	 * @return
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the string used for source indentation
	 * @param value
	 */
	public void setIndent(String value) {
		indent = (value != null) ? value : "";
	}

	/**
	 * Gets the string used for line endings
	 * @return
	 */
	public String getNewline() {
		return newline;
	}

	/**
	 * Sets the string used for line endings
	 * @param value
	 */
	public void setNewline(String value) {
		newline = (value != null) ? value : "";
	}

	/**
	 * Gets if line endings and tabs from original document
	 * should be normalized to their settings values
	 * @return
	 */
	public boolean getConvertLineEndings() {
		return convertLineEndings;
	}

	/**
	 * Sets if line endings and tabs from original document
	 * should be normalized to their settings values
	 * @param value
	 */
	public void setConvertLineEndings(boolean value) {
		convertLineEndings = value;
	}

	/**
	 * Gets if emitted markup should follow XHTML style (vs. HTML5 style)
	 * @return
	 */
	public boolean getXHTMLStyle() {
		return xhtmlStyle;
	}

	/**
	 * Sets if emitted markup should follow XHTML style (vs. HTML5 style)
	 * @param value
	 */
	public void setXHTMLStyle(boolean value) {
		xhtmlStyle = value;
	}

	/**
	 * Gets if characters above ASCII should always be encoded in HTML
	 * @return
	 */
	public boolean getEncodeNonASCII() {
		return encodeNonASCII;
	}

	/**
	 * Sets if characters above ASCII should always be encoded in HTML
	 * @param value
	 */
	public void setEncodeNonASCII(boolean value) {
		encodeNonASCII = value;
	}

	/**
	 * Gets if script tags include verbose type="text/javascript" attributes
	 * @return
	 */
	public boolean getScriptTypeAttr() {
		return scriptTypeAttr;
	}

	/**
	 * Sets if script tags include verbose type="text/javascript" attributes
	 * @param value
	 */
	public void setScriptTypeAttr(boolean value) {
		scriptTypeAttr = value;
	}

	/**
	 * Gets if all whitespace literals should be normalized (replaced by single space)
	 * @return
	 */
	public boolean getNormalizeWhitespace() {
		return normalizeWhitespace;
	}

	/**
	 * Sets if all whitespace literals should be normalized (replaced by single space)
	 * @param value
	 */
	public void setNormalizeWhitespace(boolean value) {
		normalizeWhitespace = value;
	}

	/**
	 * Gets if client paths and filenames are forced to lowercase
	 * @return
	 */
	public boolean getLowercaseClientPaths() {
		return lowercaseClientPaths;
	}

	/**
	 * Sets if client paths and filenames are forced to lowercase
	 * @param value
	 */
	public void setLowercaseClientPaths(boolean value) {
		lowercaseClientPaths = value;
	}

	String getServerName(String viewName) {
		viewName = (viewName != null) ? viewName.trim() : "";

		if ((serverPrefix == null) || (serverPrefix.length() < 1)) {
			return viewName;
		}
		return serverPrefix+NAMESPACE_DELIM+viewName;
	}

	public String getServerPath(String viewName, CodeGenerator codegen) {
		if (viewName == null) {
			throw new NullPointerException("viewName");
		}
		if (codegen == null) {
			throw new NullPointerException("codegen");
		}
		return getServerName(viewName).replace(NAMESPACE_DELIM, DIR_DELIM) + codegen.getFileExtension();
	}

	String getClientName(String viewName) {
		if (viewName != null) {
			viewName = viewName.trim();
		}

		if ((clientPrefix == null) || (clientPrefix.length() < 1)) {
			return viewName;
		}
		return clientPrefix+NAMESPACE_DELIM+viewName;
	}

	public String getClientPath(String viewName) {
		if (viewName == null) {
			throw new NullPointerException("viewName");
		}

		String clientPath = getClientName(viewName);
		if (lowercaseClientPaths) {
			// important for case-sensitive web servers & file systems
			clientPath = clientPath.toLowerCase(Locale.ROOT);
		}
		return clientPath.replace(NAMESPACE_DELIM, DIR_DELIM);
	}
}
