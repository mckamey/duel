package org.duelengine.duel.ast;

public class DocTypeNode extends BlockNode {

	public static final String BEGIN = "<!doctype"; 
	public static final String END = ">"; 

	public DocTypeNode(String value, int index, int line, int column) {
		super(BEGIN, END, value, index, line, column);
	}

	public DocTypeNode(String value) {
		super(BEGIN, END, value);
	}
}
