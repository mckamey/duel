package org.duelengine.duel.parsing;

/**
 * Represents compilation errors related to a specific token in the source file
 */
@SuppressWarnings("serial")
public class InvalidTokenException extends SyntaxException {

	private final DuelToken token;

	public InvalidTokenException(String message, DuelToken token) {
		super(message,
			(token != null) ? token.getIndex() : -1,
			(token != null) ? token.getLine() : -1,
			(token != null) ? token.getColumn() : -1);

		this.token = token;
	}

	public InvalidTokenException(String message, DuelToken token, Throwable cause) {
		super(message,
			(token != null) ? token.getIndex() : -1,
			(token != null) ? token.getLine() : -1,
			(token != null) ? token.getColumn() : -1,
			cause);

		this.token = token;
	}

	public DuelToken getToken() {
		return this.token;
	}
}
