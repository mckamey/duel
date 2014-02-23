package org.duelengine.duel.ast;

import java.util.ArrayList;
import java.util.List;

public class ContainerNode extends DuelNode {
	private final List<DuelNode> children = new ArrayList<DuelNode>();

	public ContainerNode(int index, int line, int column) {
		super(index, line, column);
	}

	protected ContainerNode(DuelNode... children) {
		if (children != null) {
			for (DuelNode child : children) {
				appendChild(child);
			}
		}
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public int childCount() {
		return children.size();
	}

	public List<DuelNode> getChildren() {
		return children;
	}

	public DuelNode getFirstChild() {
		return children.isEmpty() ? null : children.get(0);
	}

	public DuelNode getLastChild() {
		return children.isEmpty() ? null : children.get(children.size()-1);
	}

	public void appendChild(DuelNode child) {
		children.add(child);
		child.setParent(this);
	}

	public boolean removeChild(DuelNode oldChild) {
		if (oldChild == null) {
			return false;
		}

		for (int i=0, length=children.size(); i<length; i++) {
			DuelNode child = children.get(i);
			if (child == oldChild) {
				children.remove(i);
				child.setParent(null);
				return true;
			}
		}

		return false;
	}

	public boolean replaceChild(DuelNode newChild, DuelNode oldChild) {
		if (oldChild == null) {
			appendChild(newChild);
			return true;
		}

		for (int i=0, length=children.size(); i<length; i++) {
			DuelNode child = children.get(i);
			if (child == oldChild) {
				children.set(i, newChild);
				newChild.setParent(this);
				child.setParent(null);
				return true;
			}
		}

		return false;
	}

	StringBuilder toString(StringBuilder buffer) {
		if (!children.isEmpty()) {
			for (DuelNode child : children) {
				buffer.append(child);
			}
		}

		return buffer;
	}

	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ContainerNode)) {
			// includes null
			return false;
		}

		ContainerNode that = (ContainerNode)arg;
		if (children.size() != that.children.size()) {
			return false;
		}

		for (int i=0, length=children.size(); i<length; i++) {
			DuelNode a = children.get(i);
			DuelNode b = that.children.get(i);
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
		for (DuelNode child : children) {
			if (child == null) {
				continue;
			}
			hash = hash * HASH_PRIME + child.hashCode();
		}
		return hash;
	}
}
