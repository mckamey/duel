package org.duelengine.duel.codedom;

public class CodeUnaryOperatorExpression extends CodeExpression {

	private CodeUnaryOperatorType operator;
	private CodeExpression expression;

	public CodeUnaryOperatorExpression() {
	}

	public CodeUnaryOperatorExpression(CodeUnaryOperatorType op, CodeExpression expr) {
		operator = op;
		expression = expr;
	}

	public CodeUnaryOperatorType getOperator() {
		return operator;
	}

	public void setOperator(CodeUnaryOperatorType value) {
		operator = value;
	}

	public CodeExpression getExpression() {
		return expression;
	}
	
	public void setLeft(CodeExpression value) {
		expression = value;
	}

	@Override
	public Class<?> getResultType() {
		switch (operator) {
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
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (expression != null) {
				expression.visit(visitor);
			}
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

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode() * HASH_PRIME + operator.hashCode();
		if (expression != null) {
			hash = hash * HASH_PRIME + expression.hashCode();
		}
		return hash;
	}
}
