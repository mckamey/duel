package org.duelengine.duel.ast;

public class CodeCommentNode extends BlockNode {

	public static final String BEGIN = "<%--"; 
	public static final String END = "--%>"; 

	public CodeCommentNode(String value, int index, int line, int column) {
		super(BEGIN, END, value, index, line, column);
	}

	public CodeCommentNode(String value) {
		super(BEGIN, END, value);
	}
}
