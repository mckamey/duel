package org.duelengine.duel.codedom;

import java.util.Arrays;

public class CodeConditionStatement extends CodeStatement {

	private CodeExpression condition;
	private final CodeStatementCollection trueStatements = new CodeStatementCollection();
	private final CodeStatementCollection falseStatements = new CodeStatementCollection();

	public CodeConditionStatement() {
	}

	public CodeConditionStatement(CodeExpression condition, CodeStatement... trueStatements) {
		this(condition, trueStatements, null);
	}

	public CodeConditionStatement(CodeExpression condition, CodeStatement[] trueStatements, CodeStatement[] falseStatements) {
		this.condition = condition;

		if (trueStatements != null) {
			this.trueStatements.addAll(trueStatements);
		}

		if (falseStatements != null) {
			this.falseStatements.addAll(falseStatements);
		}
	}

	public CodeExpression getCondition() {
		return this.condition;
	}

	public void setCondition(CodeExpression value) {
		this.condition = value;
	}

	public CodeStatementCollection getTrueStatements() {
		return this.trueStatements;
	}

	public CodeStatementCollection getFalseStatements() {
		return this.falseStatements;
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

		int hash = this.trueStatements.hashCode() * HASH_PRIME + this.falseStatements.hashCode();
		if (this.condition != null) {
			hash = hash * HASH_PRIME + this.condition.hashCode();
		}
		return hash;
	}
}
