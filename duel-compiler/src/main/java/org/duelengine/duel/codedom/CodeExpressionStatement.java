package org.duelengine.duel.codedom;

public class CodeExpressionStatement extends CodeStatement {

	private CodeExpression expression;

	public CodeExpressionStatement() {
	}

	public CodeExpressionStatement(CodeExpression expression) {
		this.expression = expression;
	}

	public CodeExpression getExpression() {
		return expression;
	}

	public void setIdent(CodeExpression value) {
		expression = value;
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
		if (!(arg instanceof CodeExpressionStatement)) {
			// includes null
			return false;
		}

		CodeExpressionStatement that = (CodeExpressionStatement)arg;
		return (this.expression == null ? that.expression == null : this.expression.equals(that.expression));
	}

	@Override
	public int hashCode() {
		return (expression == null) ? 0 : expression.hashCode();
	}
}
