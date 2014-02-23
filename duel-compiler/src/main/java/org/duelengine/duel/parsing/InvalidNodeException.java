package org.duelengine.duel.parsing;

import org.duelengine.duel.ast.DuelNode;

/**
 * Represents compilation errors related to a specific node in the resulting AST
 */
@SuppressWarnings("serial")
public class InvalidNodeException extends SyntaxException {

	private final DuelNode node;

	public InvalidNodeException(String message, DuelNode duelNode) {
		super(message,
			(duelNode != null) ? duelNode.getIndex() : -1,
			(duelNode != null) ? duelNode.getLine() : -1,
			(duelNode != null) ? duelNode.getColumn() : -1);

		node = duelNode;
	}

	public InvalidNodeException(String message, DuelNode duelNode, Throwable cause) {
		super(message,
			(duelNode != null) ? duelNode.getIndex() : -1,
			(duelNode != null) ? duelNode.getLine() : -1,
			(duelNode != null) ? duelNode.getColumn() : -1,
			cause);

		node = duelNode;
	}

	public InvalidNodeException(String message, int index, int line, int column, DuelNode duelNode, Throwable cause) {
		super(message, index, line, column, cause);

		node = duelNode;
	}

	public DuelNode getNode() {
		return node;
	}
}
