package org.duelengine.duel.parsing;

/**
 * Represents compilation errors related to a specific token in the source file
 */
@SuppressWarnings("serial")
public class InvalidTokenException extends SyntaxException {

	private final DuelToken token;

	public InvalidTokenException(String message, DuelToken duelToken) {
		super(message,
			(duelToken != null) ? duelToken.getIndex() : -1,
			(duelToken != null) ? duelToken.getLine() : -1,
			(duelToken != null) ? duelToken.getColumn() : -1);

		token = duelToken;
	}

	public InvalidTokenException(String message, DuelToken duelToken, Throwable cause) {
		super(message,
			(duelToken != null) ? duelToken.getIndex() : -1,
			(duelToken != null) ? duelToken.getLine() : -1,
			(duelToken != null) ? duelToken.getColumn() : -1,
			cause);

		token = duelToken;
	}

	public DuelToken getToken() {
		return token;
	}
}
