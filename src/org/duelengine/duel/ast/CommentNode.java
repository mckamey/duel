package org.duelengine.duel.ast;

public class CommentNode extends BlockNode {

	public static final String BEGIN = "<!--"; 
	public static final String END = "-->"; 

	public CommentNode() {
		super(BEGIN, END, null);
	}

	public CommentNode(String value) {
		super(BEGIN, END, value);
	}
}
