package org.duelengine.duel.ast;

public abstract class CodeBlockNode extends BlockNode {

	protected CodeBlockNode(String begin, String end, String value) {
		super(begin, end, value);
	}

	public abstract String getClientCode();
}
