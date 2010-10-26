package org.duelengine.duel.ast;

import java.util.*;

public class ContainerNode extends Node {
	private final List<Node> children = new ArrayList<Node>();

	public ContainerNode() {
	}

	public ContainerNode(Node[] children) {
		this((children != null) ? Arrays.asList(children) : null);
	}

	public ContainerNode(Collection<Node> children) {
		if (children != null) {
			for (Node child : children) {
				this.appendChild(child);
			}
		}
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public List<Node> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public Node getFirstChild() {
		return this.children.isEmpty() ? null : this.children.get(0);
	}

	public Node getLastChild() {
		return this.children.isEmpty() ? null : this.children.get(this.children.size()-1);
	}

	public void appendChild(Node child) {
		this.children.add(child);
		child.setParent(this);
	}

	StringBuilder toString(StringBuilder buffer) {
		if (!this.children.isEmpty()) {
			for (Node child : this.children) {
				buffer.append(child);
			}
		}

		return buffer;
	}

	@Override
	public String toString() {
		return this.toString(new StringBuilder()).toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ContainerNode)) {
			// includes null
			return false;
		}

		ContainerNode that = (ContainerNode)arg;
		if (this.children.size() != that.children.size()) {
			return false;
		}

		for (int i=0, length=this.children.size(); i<length; i++) {
			Node a = this.children.get(i);
			Node b = that.children.get(i);
			if (a == null ? b != null : !a.equals(b)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = 0;
		for (Node child : this.children) {
			if (child == null) {
				continue;
			}
			hash = hash * HASH_PRIME + child.hashCode();
		}
		return hash;
	}
}
