package org.duelengine.duel.parsing;

import org.duelengine.duel.ast.DuelNode;

/**
 * Represents compilation errors related to a specific node in the resulting AST
 */
@SuppressWarnings("serial")
public class InvalidNodeException extends SyntaxException {

	private final DuelNode node;

	public InvalidNodeException(String message, DuelNode node) {
		super(message,
			(node != null) ? node.getIndex() : -1,
			(node != null) ? node.getLine() : -1,
			(node != null) ? node.getColumn() : -1);

		this.node = node;
	}

	public InvalidNodeException(String message, DuelNode node, Throwable cause) {
		super(message,
			(node != null) ? node.getIndex() : -1,
			(node != null) ? node.getLine() : -1,
			(node != null) ? node.getColumn() : -1,
			cause);

		this.node = node;
	}

	public InvalidNodeException(String message, int index, int line, int column, DuelNode node, Throwable cause) {
		super(message, index, line, column, cause);

		this.node = node;
	}

	public DuelNode getNode() {
		return this.node;
	}
}
