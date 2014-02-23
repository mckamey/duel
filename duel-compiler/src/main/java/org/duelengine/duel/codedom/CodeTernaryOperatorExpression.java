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
		testExpression = testExpr;
		trueExpression = trueExpr;
		falseExpression = falseExpr;
	}

	public CodeExpression getTestExpression() {
		return testExpression;
	}

	public void setTestExpression(CodeExpression value) {
		testExpression = value;
	}

	public CodeExpression getTrueExpression() {
		return trueExpression;
	}
	
	public void setTrueExpression(CodeExpression value) {
		trueExpression = value;
	}

	public CodeExpression getFalseExpression() {
		return falseExpression;
	}

	public void setFalseExpression(CodeExpression value) {
		falseExpression = value;
	}

	@Override
	public Class<?> getResultType() {
		if (trueExpression != null && !Object.class.equals(trueExpression.getResultType())) {
			return trueExpression.getResultType();

		} else if (falseExpression != null) {
			return falseExpression.getResultType();
		} 

		return Object.class;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (testExpression != null) {
				testExpression.visit(visitor);
			}
			if (trueExpression != null) {
				trueExpression.visit(visitor);
			}
			if (falseExpression != null) {
				falseExpression.visit(visitor);
			}
		}
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

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (testExpression != null) {
			hash = hash * HASH_PRIME + testExpression.hashCode();
		}
		if (trueExpression != null) {
			hash = hash * HASH_PRIME + trueExpression.hashCode();
		}
		if (falseExpression != null) {
			hash = hash * HASH_PRIME + falseExpression.hashCode();
		}
		return hash;
	}
}
