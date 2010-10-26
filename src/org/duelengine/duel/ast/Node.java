package org.duelengine.duel.ast;

public abstract class Node {

	private ElementNode parent;

	public ElementNode getParent() {
		return this.parent;
	}

	void setParent(ElementNode parent) {
		this.parent = parent;
	}
}
