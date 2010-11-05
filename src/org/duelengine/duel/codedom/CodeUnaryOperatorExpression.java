package org.duelengine.duel.codedom;

public class CodeUnaryOperatorExpression extends CodeExpression {

	private CodeUnaryOperatorType operator;
	private CodeExpression expression;

	public CodeUnaryOperatorExpression() {
	}

	public CodeUnaryOperatorExpression(CodeUnaryOperatorType op, CodeExpression expr) {
		this.operator = op;
		this.expression = expr;
	}

	public CodeUnaryOperatorType getOperator() {
		return this.operator;
	}

	public void setOperator(CodeUnaryOperatorType value) {
		this.operator = value;
	}

	public CodeExpression getExpression() {
		return this.expression;
	}
	
	public void setLeft(CodeExpression value) {
		this.expression = value;
	}

	@Override
	public Class<?> getResultType() {
		switch (this.operator) {
			case POSITIVE:
			case NEGATION:
			case PRE_INCREMENT:
			case PRE_DECREMENT:
			case POST_INCREMENT:
			case POST_DECREMENT:
			case BITWISE_NEGATION:
				return Number.class;

			case LOGICAL_NEGATION:
				return Boolean.class;

			default:
				return Object.class;
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeUnaryOperatorExpression)) {
			// includes null
			return false;
		}

		CodeUnaryOperatorExpression that = (CodeUnaryOperatorExpression)arg;
		if (this.operator != that.operator) {
			return false;
		}

		if (this.expression == null ? that.expression != null : !this.expression.equals(that.expression)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.operator.hashCode();
		if (this.expression != null) {
			hash = hash * HASH_PRIME + this.expression.hashCode();
		}
		return hash;
	}
}
