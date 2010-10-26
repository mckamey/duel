package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.DuelToken;

public class LiteralNode extends Node {

	private String value;

	public LiteralNode(String value) {
		this.value = value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return new StringBuilder(this.value).toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof LiteralNode)) {
			// includes null
			return false;
		}

		LiteralNode that = (LiteralNode)arg;
		return (this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		int hash = 0;
		if (this.value != null) {
			hash = this.value.hashCode();
		}
		return hash;
	}
}
