package org.duelengine.duel.ast;

public class DocTypeNode extends BlockNode {

	public static final String BEGIN = "<!doctype"; 
	public static final String END = ">"; 

	public DocTypeNode() {
		this(null);
	}

	public DocTypeNode(String value) {
		super.setBegin(BEGIN);
		super.setEnd(END);
		this.setValue(value);
	}

	@Override
	public final void setBegin(String value) {}

	@Override
	public final void setEnd(String value) {}
}
