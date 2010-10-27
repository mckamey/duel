package org.duelengine.duel.ast;

public class DocTypeNode extends BlockNode {

	public static final String BEGIN = "<!doctype"; 
	public static final String END = ">"; 

	public DocTypeNode() {
		super(BEGIN, END, null);
	}

	public DocTypeNode(String value) {
		super(BEGIN, END, value);
	}
}
