package org.duelengine.duel.ast;

public class UnknownNode extends LiteralNode {

	public UnknownNode(String value, int index, int line, int column) {
		super(value, index, line, column);
	}

	public UnknownNode(String value) {
		super(value);
	}
}
