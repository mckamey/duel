package org.duelengine.duel.codedom;

import java.util.Arrays;

/**
 * Represents a for loop
 */
public class CodeIterationStatement extends CodeStatement {

	private CodeStatement initStatement;
	private CodeStatement testStatement;
	private CodeStatement incStatement;
	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeIterationStatement() {
	}

	public CodeIterationStatement(CodeStatement initStatement, CodeStatement testStatement, CodeStatement incStatement, CodeStatement[] statements) {
		this.initStatement = initStatement;
		this.testStatement = testStatement;
		this.incStatement = incStatement;
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

	public CodeStatement getTestStatement() {
		return this.testStatement;
	}

	public void setTestStatement(CodeStatement value) {
		this.testStatement = value;
	}

	public CodeStatement getIncStatement() {
		return this.incStatement;
	}

	public void setIncStatement(CodeStatement value) {
		this.incStatement = value;
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
		if (this.testStatement == null ? that.testStatement != null : !this.testStatement.equals(that.testStatement)) {
			return false;
		}
		if (this.incStatement == null ? that.incStatement != null : !this.incStatement.equals(that.incStatement)) {
			return false;
		}
		if (!this.statements.equals(that.statements)) {
			return false;
		}
		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.statements.hashCode();
		if (this.initStatement != null) {
			hash = hash * HASH_PRIME + this.initStatement.hashCode();
		}
		if (this.testStatement != null) {
			hash = hash * HASH_PRIME + this.testStatement.hashCode();
		}
		if (this.incStatement != null) {
			hash = hash * HASH_PRIME + this.incStatement.hashCode();
		}
		return hash;
	}
}
