package org.duelengine.duel.ast;

public class CommentNode extends SpecialNode {

	private static final String NAME = "!";
	public static final String BEGIN = "<!--";
	public static final String END = "-->";

	public CommentNode(String value, int index, int line, int column) {
		super(NAME, BEGIN, END, value, index, line, column);
	}

	public CommentNode(String value) {
		super(NAME, BEGIN, END, value);
	}
}
