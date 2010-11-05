package org.duelengine.duel.codedom;

public class CodeBinaryOperatorExpression extends CodeExpression {

	private CodeBinaryOperatorType operator;
	private CodeExpression left;
	private CodeExpression right;

	public CodeBinaryOperatorExpression() {
	}

	public CodeBinaryOperatorExpression(CodeBinaryOperatorType op, CodeExpression left, CodeExpression right) {
		this.operator = op;
		this.left = left;
		this.right = right;
	}

	public CodeBinaryOperatorType getOperator() {
		return this.operator;
	}

	public void setOperator(CodeBinaryOperatorType value) {
		this.operator = value;
	}

	public CodeExpression getRight() {
		return this.right;
	}

	public void setRight(CodeExpression value) {
		this.right = value;
	}

	public CodeExpression getLeft() {
		return this.left;
	}
	
	public void setLeft(CodeExpression value) {
		this.left = value;
	}

	@Override
	public Class<?> getResultType() {
		switch (this.operator) {
			case ADD:
			case ADD_ASSIGN:
			case SUBTRACT:
			case SUBTRACT_ASSIGN:
			case MULTIPLY:
			case MULTIPLY_ASSIGN:
			case DIVIDE:
			case DIVIDE_ASSIGN:
			case MODULUS:
			case MODULUS_ASSIGN:
			case SHIFT_LEFT:
			case SHIFT_LEFT_ASSIGN:
			case SHIFT_RIGHT:
			case SHIFT_RIGHT_ASSIGN:
			case USHIFT_RIGHT:
			case USHIFT_RIGHT_ASSIGN:
			case BITWISE_AND:
			case BITWISE_AND_ASSIGN:
			case BITWISE_OR:
			case BITWISE_OR_ASSIGN:
			case BITWISE_XOR:
			case BITWISE_XOR_ASSIGN:
				return Number.class;

			case BOOLEAN_AND:
			case BOOLEAN_OR:
			case GREATER_THAN:
			case GREATER_THAN_OR_EQUAL:
			case LESS_THAN:
			case LESS_THAN_OR_EQUAL:
			case IDENTITY_EQUALITY:
			case IDENTITY_INEQUALITY:
			case VALUE_EQUALITY:
			case VALUE_INEQUALITY:
				return Boolean.class;

			case ASSIGN:
				if (this.left != null && !this.left.getResultType().equals(Object.class)) {
					return this.left.getResultType();

				} else if (this.right != null) {
					return this.right.getResultType();
				} 

				return Object.class;

			default:
				return Object.class;
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeBinaryOperatorExpression)) {
			// includes null
			return false;
		}

		CodeBinaryOperatorExpression that = (CodeBinaryOperatorExpression)arg;
		if (this.operator != that.operator) {
			return false;
		}

		if (this.left == null ? that.left != null : !this.left.equals(that.left)) {
			return false;
		}

		if (this.right == null ? that.right != null : !this.right.equals(that.right)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.operator.hashCode();
		if (this.left != null) {
			hash = hash * HASH_PRIME + this.left.hashCode();
		}
		if (this.right != null) {
			hash = hash * HASH_PRIME + this.right.hashCode();
		}
		return hash;
	}
}
