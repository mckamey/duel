package org.duelengine.duel.parsing;

/**
 * Provides simplified definitions of character classes
 */
final class CharUtility {

	public static boolean isWhiteSpace(int ch) {
		switch (ch) {
			case ' ':		// Space
			case '\t':		// Tab
			case '\n':		// LF
			case '\r':		// CR
			case '\u000C':	// FF
				return true;
			default:
				return false;
		}
	}

	public static boolean isLetter(int ch) {
		return
			((ch >= 'a') && (ch <= 'z')) ||
			((ch >= 'A') && (ch <= 'Z'));
	}

	public static boolean isDigit(int ch) {
		return (ch >= '0') && (ch <= '9');
	}

	public static boolean isHexDigit(int ch) {
		return
			(ch >= '0' && ch <= '9') ||
			(ch >= 'a' && ch <= 'f') ||
			(ch >= 'A' && ch <= 'F');
	}

	/**
	 * Checks for HTML name start char
	 */
	public static boolean isNameStartChar(int ch) {
		// http://www.w3.org/TR/xml/#sec-common-syn
		return
			(ch >= 'a' && ch <= 'z') ||
			(ch >= 'A' && ch <= 'Z') ||
			(ch == ':') ||
			(ch == '_') ||
			(ch >= '\u00C0' && ch <= '\u00D6') ||
			(ch >= '\u00D8' && ch <= '\u00F6') ||
			(ch >= '\u00F8' && ch <= '\u02FF') ||
			(ch >= '\u0370' && ch <= '\u037D') ||
			(ch >= '\u037F' && ch <= '\u1FFF') ||
			(ch >= '\u200C' && ch <= '\u200D') ||
			(ch >= '\u2070' && ch <= '\u218F') ||
			(ch >= '\u2C00' && ch <= '\u2FEF') ||
			(ch >= '\u3001' && ch <= '\uD7FF') ||
			(ch >= '\uF900' && ch <= '\uFDCF') ||
			(ch >= '\uFDF0' && ch <= '\uFFFD');
			//(ch >= '\u10000' && ch <= '\uEFFFF');
	}

	/**
	 * Checks for HTML name char
	 */
	public static boolean isNameChar(int ch)
	{
		// http://www.w3.org/TR/xml/#sec-common-syn
		return
			CharUtility.isNameStartChar(ch) ||
			(ch >= '0' && ch <= '9') ||
			(ch == '-') ||
			(ch == '.') ||
			(ch == '\u00B7') ||
			(ch >= '\u0300' && ch <= '\u036F') ||
			(ch >= '\u203F' && ch <= '\u2040');
	}
}
