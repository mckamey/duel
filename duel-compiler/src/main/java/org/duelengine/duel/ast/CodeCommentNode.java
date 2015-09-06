package org.duelengine.duel.ast;

public class CodeCommentNode extends SpecialNode {

	private static final String NAME = "!";
	public static final String BEGIN = "<%--";
	public static final String END = "--%>";

	public CodeCommentNode(String value, int index, int line, int column) {
		super(NAME, BEGIN, END, value, index, line, column);
	}

	public CodeCommentNode(String value) {
		super(NAME, BEGIN, END, value);
	}
}
