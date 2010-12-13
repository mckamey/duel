package org.duelengine.duel.codedom;

public class CodeMethodReturnStatement extends CodeStatement {

	private CodeExpression expression;

	public CodeMethodReturnStatement() {
	}

	public CodeMethodReturnStatement(CodeExpression expression) {
		this.expression = expression;
	}

	public CodeExpression getExpression() {
		return expression;
	}

	public void setExpression(CodeExpression value) {
		this.expression = value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (this.expression != null) {
				this.expression.visit(visitor);
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeMethodReturnStatement)) {
			// includes null
			return false;
		}

		CodeMethodReturnStatement that = (CodeMethodReturnStatement)arg;
		return (this.expression == null ? that.expression == null : this.expression.equals(that.expression));
	}

	@Override
	public int hashCode() {
		return (this.expression == null) ? 0 : this.expression.hashCode();
	}
}
