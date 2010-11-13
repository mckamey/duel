package org.duelengine.duel.codedom;

/**
 * Represents a built-in JavaScript object property
 */
public class ScriptPropertyExpression extends ScriptExpression {

	private String propertyName;

	public ScriptPropertyExpression() {
	}

	public ScriptPropertyExpression(String value) {
		this.propertyName = value;
	}

	public String getValue() {
		return this.propertyName;
	}

	public void setValue(String value) {
		this.propertyName = value;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ScriptPropertyExpression)) {
			// includes null
			return false;
		}

		ScriptPropertyExpression that = (ScriptPropertyExpression)arg;
		if (this.propertyName == null ? that.propertyName != null : !this.propertyName.equals(that.propertyName)) {
			return false;
		}

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.propertyName != null) {
			hash = hash * HASH_PRIME + this.propertyName.hashCode();
		}
		return hash;
	}
}
