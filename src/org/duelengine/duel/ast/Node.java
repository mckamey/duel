package org.duelengine.duel.ast;

public abstract class Node {

	private ContainerNode parent;

	public ContainerNode getParent() {
		return this.parent;
	}

	void setParent(ContainerNode parent) {
		this.parent = parent;
	}
}
