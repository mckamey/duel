package org.duelengine.duel.ast;

public class AttributeNode {

	private String name;
	private Node value;

	public AttributeNode() {
	}

	public AttributeNode(String name, Node value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setValue(Node value) {
		this.value = value;
	}

	public Node getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(this.name)
				.append("=\"")
				.append(this.value)
				.append('"').toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof AttributeNode)) {
			// includes null
			return false;
		}

		AttributeNode that = (AttributeNode)arg;
		return (this.name == null ? that.name == null : this.name.equals(that.name)) &&
			(this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.name == null) ? 0 : this.name.hashCode();
		if (this.value != null) {
			hash = hash * HASH_PRIME + this.value.hashCode();
		}
		return hash;
	}
}
