package org.duelengine.duel.ast;

public abstract class DuelNode {

	private final int index;
	private final int line;
	private final int column;
	private ContainerNode parent;

	protected DuelNode() {
		this.index = -1;
		this.line = -1;
		this.column = -1;
	}

	protected DuelNode(int index, int line, int column) {
		this.index = index;
		this.line = line;
		this.column = column;
	}

	public int getIndex() {
		return index;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public ContainerNode getParent() {
		return this.parent;
	}

	void setParent(ContainerNode parent) {
		this.parent = parent;
	}
}
