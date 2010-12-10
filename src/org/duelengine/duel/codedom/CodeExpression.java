package org.duelengine.duel.codedom;

public abstract class CodeExpression extends CodeObject {

	private boolean parens;

	public abstract Class<?> getResultType();

	public void setParens(boolean value) {
		this.parens = value;
	}

	public boolean hasParens() {
		return this.parens;
	}

	public CodeExpression withParens() {
		this.parens = true;
		return this;
	}

	@Override
	public CodeExpression withUserData(Object... pairs) {
		return (CodeExpression)super.withUserData(pairs);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeExpression)) {
			// includes null
			return false;
		}

		CodeExpression that = (CodeExpression)arg;
		return (this.parens == that.parens);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
