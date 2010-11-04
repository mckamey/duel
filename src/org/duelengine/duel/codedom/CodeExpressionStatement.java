package org.duelengine.duel.codedom;

public class CodeExpressionStatement extends CodeStatement {

	private CodeExpression expr;

	public CodeExpressionStatement() {
	}

	public CodeExpressionStatement(CodeExpression expr) {
		this.expr = expr;
	}

	public CodeExpression getExpression() {
		return expr;
	}

	public void setIdent(CodeExpression value) {
		this.expr = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeExpressionStatement)) {
			// includes null
			return false;
		}

		CodeExpressionStatement that = (CodeExpressionStatement)arg;
		return (this.expr == null ? that.expr == null : this.expr.equals(that.expr));
	}

	@Override
	public int hashCode() {
		return (this.expr == null) ? 0 : this.expr.hashCode();
	}
}
