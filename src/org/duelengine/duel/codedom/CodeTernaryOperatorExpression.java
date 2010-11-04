package org.duelengine.duel.codedom;

/**
 * Represents a C-style ternary conditional expression 
 */
public class CodeTernaryOperatorExpression extends CodeExpression {

	private CodeExpression testExpression;
	private CodeExpression trueExpression;
	private CodeExpression falseExpression;

	public CodeTernaryOperatorExpression() {
	}

	public CodeTernaryOperatorExpression(CodeExpression testExpr, CodeExpression trueExpr, CodeExpression falseExpr) {
		this.testExpression = testExpr;
		this.trueExpression = trueExpr;
		this.falseExpression = falseExpr;
	}

	public CodeExpression getTestExpression() {
		return this.testExpression;
	}

	public void setTestExpression(CodeExpression value) {
		this.testExpression = value;
	}

	public CodeExpression getTrueExpression() {
		return this.trueExpression;
	}
	
	public void setTrueExpression(CodeExpression value) {
		this.trueExpression = value;
	}

	public CodeExpression getFalseExpression() {
		return this.falseExpression;
	}

	public void setFalseExpression(CodeExpression value) {
		this.falseExpression = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeTernaryOperatorExpression)) {
			// includes null
			return false;
		}

		CodeTernaryOperatorExpression that = (CodeTernaryOperatorExpression)arg;

		if (this.testExpression == null ? that.testExpression != null : !this.testExpression.equals(that.testExpression)) {
			return false;
		}

		if (this.trueExpression == null ? that.trueExpression != null : !this.trueExpression.equals(that.trueExpression)) {
			return false;
		}

		if (this.falseExpression == null ? that.falseExpression != null : !this.falseExpression.equals(that.falseExpression)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = 0;
		if (this.trueExpression != null) {
			hash = hash * HASH_PRIME + this.trueExpression.hashCode();
		}
		if (this.falseExpression != null) {
			hash = hash * HASH_PRIME + this.falseExpression.hashCode();
		}
		return hash;
	}
}
