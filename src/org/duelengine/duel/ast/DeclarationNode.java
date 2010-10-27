package org.duelengine.duel.ast;

public class DeclarationNode extends CodeBlockNode {

	public static final String BEGIN = "<%@";
	public static final String END = "%>";

	public DeclarationNode() {
		super(BEGIN, END, null);
	}

	public DeclarationNode(String expression) {
		super(BEGIN, END, expression);
	}

	@Override
	public String getCode() {
		// TODO
		return null;
	}
}
