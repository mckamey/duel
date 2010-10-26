package org.duelengine.duel.ast;

import java.util.*;

public class ElementNode extends Node {

	private String tagName;
	private final Map<String, Node> attributes = new LinkedHashMap<String, Node>();
	private final List<Node> children = new ArrayList<Node>();

	public ElementNode() {
	}

	public ElementNode(String name) {
		this.tagName = name;
	}

	public ElementNode(String name, AttributeNode[] attr) {
		this(name, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public ElementNode(String name, Collection<AttributeNode> attr) {
		this(name, attr, null);
	}

	public ElementNode(String name, AttributeNode[] attr, Node[] children) {
		this(name, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public ElementNode(String name, Collection<AttributeNode> attr, Collection<Node> children) {
		this.tagName = name;

		if (attr != null) {
			for (AttributeNode a : attr) {
				this.attributes.put(a.getName(), a.getValue());
			}
		}

		if (children != null) {
			for (Node child : children) {
				this.appendChild(child);
			}
		}
	}

	public String getTagName() {
		return this.tagName;
	}

	public void setTagName(String value) {
		this.tagName = value;
	}

	public void addAttribute(AttributeNode attr) {
		this.attributes.put(attr.getName(), attr.getValue());
	}

	public Node getAttribute(String name) {
		return this.attributes.get(name);
	}

	public void setAttribute(String name, Node value) {
		this.attributes.put(name, value);
	}

	public void clearAttributes() {
		this.attributes.clear();
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

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder('<').append(this.tagName);

		for (String name : this.attributes.keySet()) {
			buffer
				.append(' ')
				.append(name)
				.append("=\"")
				.append(this.getAttribute(name))
				.append('"');
		}

		if (this.children.isEmpty()) {
			return buffer.append(" />").toString();
		}
		buffer.append('>');

		for (Node child : this.children) {
			buffer.append(child);
		}		
		
		return buffer.append("</").append(this.tagName).append('>').toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ElementNode)) {
			// includes null
			return false;
		}

		ElementNode that = (ElementNode)arg;
		if (this.tagName == null ? that.tagName != null : !this.tagName.equals(that.tagName)) {
			return false;
		}

		for (String name : this.attributes.keySet()) {
			if (!that.attributes.containsKey(name)) {
				return false;
			}

			Node thisValue = this.attributes.get(name);
			Node thatValue = that.attributes.get(name);

			if (thisValue == null ? thatValue != null : !thisValue.equals(thatValue)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.tagName == null) ? 0 : this.tagName.hashCode();
		if (this.attributes != null) {
			hash = hash * HASH_PRIME + this.attributes.hashCode();
		}
		return hash;
	}
}
