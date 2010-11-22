package org.duelengine.duel.ast;

import java.util.*;

public class ContainerNode extends Node {
	private final List<Node> children = new ArrayList<Node>();

	public ContainerNode(int index, int line, int column) {
		super(index, line, column);
	}

	protected ContainerNode(Node... children) {
		if (children != null) {
			for (Node child : children) {
				this.appendChild(child);
			}
		}
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public int childCount() {
		return this.children.size();
	}

	public List<Node> getChildren() {
		return this.children;
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

	public boolean removeChild(Node oldChild) {
		if (oldChild == null) {
			return false;
		}

		for (int i=0, length=this.children.size(); i<length; i++) {
			Node child = this.children.get(i);
			if (child == oldChild) {
				this.children.remove(i);
				child.setParent(null);
				return true;
			}
		}

		return false;
	}

	public boolean replaceChild(Node newChild, Node oldChild) {
		if (oldChild == null) {
			this.appendChild(newChild);
			return true;
		}

		for (int i=0, length=this.children.size(); i<length; i++) {
			Node child = this.children.get(i);
			if (child == oldChild) {
				this.children.set(i, newChild);
				newChild.setParent(this);
				child.setParent(null);
				return true;
			}
		}

		return false;
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
