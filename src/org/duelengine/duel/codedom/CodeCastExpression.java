package org.duelengine.duel.codedom;

/**
 * Represents a casting operation
 */
public class CodeCastExpression extends CodeExpression {

	private Class<?> type = Object.class;
	private CodeExpression expression;

	public CodeCastExpression() {
		this.withParens();
	}

	public CodeCastExpression(Class<?> type, CodeExpression expression) {
		if (type != null) {
			this.type = type;
		}
		this.expression = expression;
		this.withParens();
	}

	@Override
	public Class<?> getResultType() {
		return this.type;
	}

	public void setResultType(Class<?> value) {
		this.type = (value == null) ? Object.class : value;
	}

	public CodeExpression getExpression() {
		return this.expression;
	}

	public void setExpression(CodeExpression value) {
		this.expression = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeCastExpression)) {
			// includes null
			return false;
		}

		CodeCastExpression that = (CodeCastExpression)arg;

		if (this.expression == null ? that.expression != null : !this.expression.equals(that.expression)) {
			return false;
		}

		return this.type.equals(that.type);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.type.hashCode();
		if (this.expression != null) {
			hash = hash * HASH_PRIME + this.expression.hashCode();
		}
		return hash;
	}
}
