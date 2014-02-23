package org.duelengine.duel.codedom;

/**
 * Represents a for loop
 */
public class CodeIterationStatement extends CodeStatement {

	private CodeStatement initStatement;
	private CodeExpression testExpression;
	private CodeStatement incrementStatement;
	private final CodeStatementCollection statements;

	public CodeIterationStatement() {
		this(null, null, null);
	}

	public CodeIterationStatement(CodeStatement initStatement, CodeExpression testExpression, CodeStatement incrementStatement, CodeStatement... statements) {
		this.initStatement = initStatement;
		this.testExpression = testExpression;
		this.incrementStatement = incrementStatement;
		this.statements = new CodeStatementCollection(this);
		if (statements != null) {
			this.statements.addAll(statements);
		}
	}

	public CodeStatement getInitStatement() {
		return initStatement;
	}

	public void setInitStatement(CodeStatement value) {
		initStatement = value;
	}

	public CodeExpression getTestExpression() {
		return testExpression;
	}

	public void setTestExpression(CodeExpression value) {
		testExpression = value;
	}

	public CodeStatement getIncrementStatement() {
		return incrementStatement;
	}

	public void setIncrementStatement(CodeStatement value) {
		incrementStatement = value;
	}

	public CodeStatementCollection getStatements() {
		return statements;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (initStatement != null) {
				initStatement.visit(visitor);
			}
			if (testExpression != null) {
				testExpression.visit(visitor);
			}
			if (incrementStatement != null) {
				incrementStatement.visit(visitor);
			}
			for (CodeStatement statement : statements) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
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

		int hash = statements.hashCode();
		if (initStatement != null) {
			hash = hash * HASH_PRIME + initStatement.hashCode();
		}
		if (testExpression != null) {
			hash = hash * HASH_PRIME + testExpression.hashCode();
		}
		if (incrementStatement != null) {
			hash = hash * HASH_PRIME + incrementStatement.hashCode();
		}
		return hash;
	}
}
