package org.duelengine.duel.ast;

public class AttributePair {

	private String attrName;
	private DuelNode attrValue;

	public AttributePair(String name, DuelNode value) {
		attrName = name;
		attrValue = value;
	}

	public void setName(String value) {
		attrName = value;
	}

	public String getName() {
		return attrName;
	}

	public void setValue(DuelNode value) {
		this.attrValue = value;
	}

	public DuelNode getValue() {
		return attrValue;
	}

	@Override
	public String toString() {
		return new StringBuilder(attrName)
				.append("=\"")
				.append(attrValue)
				.append('"').toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof AttributePair)) {
			// includes null
			return false;
		}

		AttributePair that = (AttributePair)arg;
		return (this.attrName == null ? that.attrName == null : this.attrName.equals(that.attrName)) &&
				(this.attrValue == null ? that.attrValue == null : this.attrValue.equals(that.attrValue));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (attrName == null) ? 0 : attrName.hashCode();
		if (attrValue != null) {
			hash = hash * HASH_PRIME + attrValue.hashCode();
		}
		return hash;
	}
}
