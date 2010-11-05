package org.duelengine.duel.codedom;

public abstract class CodeExpression extends CodeObject {

	private boolean hasParens;

	public abstract Class<?> getResultType();

	public void setHasParens(boolean value) {
		this.hasParens = value;
	}

	public boolean getHasParens() {
		return this.hasParens;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeExpression)) {
			// includes null
			return false;
		}

		CodeExpression that = (CodeExpression)arg;
		return (this.hasParens == that.hasParens);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
