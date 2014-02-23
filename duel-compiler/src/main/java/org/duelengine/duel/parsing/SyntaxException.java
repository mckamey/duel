package org.duelengine.duel.parsing;

/**
 * Represents compilation error related to a specific point in the source file
 */
@SuppressWarnings("serial")
public class SyntaxException extends IllegalArgumentException {

	private final int index;
	private final int line;
	private final int column;

	public SyntaxException(String message, int index, int line, int column) {
		super(message);

		this.index = index;
		this.line = line;
		this.column = column;
	}

	public SyntaxException(String message, int index, int line, int column, Throwable cause) {
		super(message, cause);

		this.index = index;
		this.line = line;
		this.column = column;
	}

	public int getIndex() {
		return index;
	}
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return column;
	}
}
