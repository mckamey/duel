package org.duelengine.duel.ast;

public class CommentNode extends BlockNode {

	public static final String BEGIN = "<!--"; 
	public static final String END = "-->"; 

	public CommentNode(String value, int index, int line, int column) {
		super(BEGIN, END, value, index, line, column);
	}

	public CommentNode(String value) {
		super(BEGIN, END, value);
	}
}
