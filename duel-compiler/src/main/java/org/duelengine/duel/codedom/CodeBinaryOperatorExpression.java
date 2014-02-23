package org.duelengine.duel.codedom;

import org.duelengine.duel.DuelData;

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
		return operator;
	}

	public void setOperator(CodeBinaryOperatorType value) {
		operator = value;
	}

	public CodeExpression getRight() {
		return right;
	}

	public void setRight(CodeExpression value) {
		right = value;
	}

	public CodeExpression getLeft() {
		return left;
	}
	
	public void setLeft(CodeExpression value) {
		left = value;
	}

	@Override
	public Class<?> getResultType() {
		switch (operator) {
			case ADD:
			case ADD_ASSIGN:
				// TODO: ADD is ambiguous
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
				if (left == null || !DuelData.isBoolean(left.getResultType()) ||
					right == null || !DuelData.isBoolean(right.getResultType())) {
					return Object.class;
				}
				return Boolean.class;

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
				if (left != null && !left.getResultType().equals(Object.class)) {
					return left.getResultType();

				} else if (right != null) {
					return right.getResultType();
				} 

				return Object.class;

			default:
				return Object.class;
		}
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (left != null) {
				left.visit(visitor);
			}
			if (right != null) {
				right.visit(visitor);
			}
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

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode()* HASH_PRIME + operator.hashCode();
		if (left != null) {
			hash = hash * HASH_PRIME + left.hashCode();
		}
		if (right != null) {
			hash = hash * HASH_PRIME + right.hashCode();
		}
		return hash;
	}
}
