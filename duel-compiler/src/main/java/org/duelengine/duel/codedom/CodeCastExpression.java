package org.duelengine.duel.codedom;

/**
 * Represents a casting operation
 */
public class CodeCastExpression extends CodeExpression {

	private Class<?> type = Object.class;
	private CodeExpression expression;

	public CodeCastExpression() {
		withParens();
	}

	public CodeCastExpression(Class<?> type, CodeExpression expression) {
		if (type != null) {
			this.type = type;
		}
		this.expression = expression;
		withParens();
	}

	public CodeExpression getExpression() {
		return expression;
	}

	public void setExpression(CodeExpression value) {
		expression = value;
	}

	@Override
	public Class<?> getResultType() {
		return type;
	}

	public void setResultType(Class<?> value) {
		type = (value == null) ? Object.class : value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (expression != null) {
				expression.visit(visitor);
			}
		}
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

		return type.equals(that.type);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = type.hashCode();
		if (expression != null) {
			hash = hash * HASH_PRIME + expression.hashCode();
		}
		return hash;
	}
}
