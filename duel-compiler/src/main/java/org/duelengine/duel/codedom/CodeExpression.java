package org.duelengine.duel.codedom;

public abstract class CodeExpression extends CodeObject {

	private boolean parens;

	public abstract Class<?> getResultType();

	public void setParens(boolean value) {
		parens = value;
	}

	public boolean hasParens() {
		return parens;
	}

	public CodeExpression withParens() {
		parens = true;
		return this;
	}

	public CodeStatement asStatement() {
		return new CodeExpressionStatement(this);
	}

	@Override
	public CodeExpression withMetaData(Object... pairs) {
		return (CodeExpression)super.withMetaData(pairs);
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
