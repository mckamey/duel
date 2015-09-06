package org.duelengine.duel.ast;

public class DocTypeNode extends SpecialNode {

	private static final String NAME = "!doctype";
	public static final String BEGIN = "<"+NAME;
	public static final String END = ">"; 

	public DocTypeNode(String value, int index, int line, int column) {
		super(NAME, BEGIN, END, value, index, line, column);
	}

	public DocTypeNode(String value) {
		super(NAME, BEGIN, END, value);
	}
}
