package org.duelengine.duel.ast;

public class LiteralNode extends DuelNode {

	private String value;

	public LiteralNode(String value, int index, int line, int column) {
		super(index, line, column);

		this.value = value;
	}

	public LiteralNode(String value) {
		this.value = value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return new StringBuilder(value).toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof LiteralNode)) {
			// includes null
			return false;
		}

		LiteralNode that = (LiteralNode)arg;
		return (value == null ? that.value == null : value.equals(that.value));
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (value != null) {
			hash = value.hashCode();
		}
		return hash;
	}
}
