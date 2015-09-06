package org.duelengine.duel.ast;

/**
 * SGML declarations, comments and other element-like nodes.
 */
public abstract class SpecialNode extends BlockNode {
	private final String name;

	protected SpecialNode(String name, String begin, String end, String value, int index, int line, int column) {
		super(begin, end, value, index, line, column);
		this.name = name;
	}

	protected SpecialNode(String name, String begin, String end, String value) {
		super(begin, end, value);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
