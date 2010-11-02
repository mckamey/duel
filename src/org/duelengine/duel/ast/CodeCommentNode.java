package org.duelengine.duel.ast;

public class CodeCommentNode extends BlockNode {

	public static final String BEGIN = "<%--"; 
	public static final String END = "--%>"; 

	public CodeCommentNode() {
		super(BEGIN, END, null);
	}

	public CodeCommentNode(String value) {
		super(BEGIN, END, value);
	}
}
