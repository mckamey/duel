package org.duelengine.duel.codedom;

public class CodeConditionStatement extends CodeStatement {

	private CodeExpression condition;
	private final CodeStatementCollection trueStatements;
	private final CodeStatementCollection falseStatements;

	public CodeConditionStatement() {
		this(null);
	}

	public CodeConditionStatement(CodeExpression condition, CodeStatement... trueStatements) {
		this(condition, trueStatements, null);
	}

	public CodeConditionStatement(CodeExpression condition, CodeStatement[] trueStatements, CodeStatement[] falseStatements) {
		this.trueStatements = new CodeStatementCollection(this);
		this.falseStatements = new CodeStatementCollection(this);

		this.condition = condition;

		if (trueStatements != null) {
			this.trueStatements.addAll(trueStatements);
		}

		if (falseStatements != null) {
			this.falseStatements.addAll(falseStatements);
		}
	}

	public CodeExpression getCondition() {
		return condition;
	}

	public void setCondition(CodeExpression value) {
		condition = value;
	}

	public CodeStatementCollection getTrueStatements() {
		return trueStatements;
	}

	public CodeStatementCollection getFalseStatements() {
		return falseStatements;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (condition != null) {
				condition.visit(visitor);
			}
			for (CodeStatement statement : trueStatements) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
			for (CodeStatement statement : falseStatements) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeConditionStatement)) {
			// includes null
			return false;
		}

		CodeConditionStatement that = (CodeConditionStatement)arg;
		if (this.condition == null ? that.condition != null : !this.condition.equals(that.condition)) {
			return false;
		}
		if (!this.trueStatements.equals(that.trueStatements)) {
			return false;
		}
		if (!this.falseStatements.equals(that.falseStatements)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = trueStatements.hashCode() * HASH_PRIME + falseStatements.hashCode();
		if (condition != null) {
			hash = hash * HASH_PRIME + condition.hashCode();
		}
		return hash;
	}
}
