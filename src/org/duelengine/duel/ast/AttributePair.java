package org.duelengine.duel.ast;

public class AttributePair {

	private String name;
	private DuelNode value;

	public AttributePair(String name, DuelNode value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setValue(DuelNode value) {
		this.value = value;
	}

	public DuelNode getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return new StringBuilder(this.name)
				.append("=\"")
				.append(this.value)
				.append('"').toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof AttributePair)) {
			// includes null
			return false;
		}

		AttributePair that = (AttributePair)arg;
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
