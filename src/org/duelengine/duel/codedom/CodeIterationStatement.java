package org.duelengine.duel.codedom;

import java.util.Arrays;

/**
 * Represents a for loop
 */
public class CodeIterationStatement extends CodeStatement {

	private CodeStatement initStatement;
	private CodeExpression testExpression;
	private CodeStatement incrementStatement;
	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeIterationStatement() {
	}

	public CodeIterationStatement(CodeStatement initStatement, CodeExpression testExpression, CodeStatement incrementStatement, CodeStatement[] statements) {
		this.initStatement = initStatement;
		this.testExpression = testExpression;
		this.incrementStatement = incrementStatement;
		if (statements != null) {
			this.statements.addAll(Arrays.asList(statements));
		}
	}

	public CodeStatement getInitStatement() {
		return this.initStatement;
	}

	public void setInitStatement(CodeStatement value) {
		this.initStatement = value;
	}

	public CodeExpression getTestStatement() {
		return this.testExpression;
	}

	public void setTestStatement(CodeExpression value) {
		this.testExpression = value;
	}

	public CodeStatement getIncrementStatement() {
		return this.incrementStatement;
	}

	public void setIncrementStatement(CodeStatement value) {
		this.incrementStatement = value;
	}

	public CodeStatementCollection getStatements() {
		return this.statements;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeIterationStatement)) {
			// includes null
			return false;
		}

		CodeIterationStatement that = (CodeIterationStatement)arg;
		if (this.initStatement == null ? that.initStatement != null : !this.initStatement.equals(that.initStatement)) {
			return false;
		}
		if (this.testExpression == null ? that.testExpression != null : !this.testExpression.equals(that.testExpression)) {
			return false;
		}
		if (this.incrementStatement == null ? that.incrementStatement != null : !this.incrementStatement.equals(that.incrementStatement)) {
			return false;
		}
		if (!this.statements.equals(that.statements)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.statements.hashCode();
		if (this.initStatement != null) {
			hash = hash * HASH_PRIME + this.initStatement.hashCode();
		}
		if (this.testExpression != null) {
			hash = hash * HASH_PRIME + this.testExpression.hashCode();
		}
		if (this.incrementStatement != null) {
			hash = hash * HASH_PRIME + this.incrementStatement.hashCode();
		}
		return hash;
	}
}
