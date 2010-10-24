package org.duelengine.duel.parsing;

/**
 * Provides simplified (ASCII) definitions of character classes
 */
final class CharUtility {

	public static boolean isWhiteSpace(int ch) {
		switch (ch) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
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
}
