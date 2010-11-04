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
