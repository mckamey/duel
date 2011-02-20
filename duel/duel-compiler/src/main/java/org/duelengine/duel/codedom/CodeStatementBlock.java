package org.duelengine.duel.codedom;

/**
 * Used internally to pass around a sequence of statements as a CodeObject
 */
public class CodeStatementBlock extends CodeObject {

	private final CodeStatementCollection statements;

	public CodeStatementBlock() {
		this.statements = new CodeStatementCollection(this);
	}

	public CodeStatementCollection getStatements() {
		return this.statements;
	}

	public void addAll(CodeStatementBlock statements) {
		this.statements.addAll(statements);
	}

	public void add(CodeExpression expression) {
		this.statements.add(expression);
	}

	public void add(CodeStatement statement) {
		this.statements.add(statement);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeStatement statement : this.statements) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
	}

	@Override
	public CodeStatementBlock withUserData(Object... pairs) {
		return (CodeStatementBlock)super.withUserData(pairs);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeStatementBlock)) {
			// includes null
			return false;
		}

		CodeStatementBlock that = (CodeStatementBlock)arg;
		return this.statements.equals(that.statements);
	}

	@Override
	public int hashCode() {
		return this.statements.hashCode();
	}
}
