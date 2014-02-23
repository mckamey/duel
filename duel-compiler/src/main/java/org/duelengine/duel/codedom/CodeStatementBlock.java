package org.duelengine.duel.codedom;

/**
 * Used internally to pass around a sequence of statements as a CodeObject
 */
public class CodeStatementBlock extends CodeObject {

	private final CodeStatementCollection statements;

	public CodeStatementBlock() {
		statements = new CodeStatementCollection(this);
	}

	public CodeStatementCollection getStatements() {
		return statements;
	}

	public void addAll(CodeStatementBlock statements) {
		statements.addAll(statements);
	}

	public void add(CodeExpression expression) {
		statements.add(expression);
	}

	public void add(CodeStatement statement) {
		statements.add(statement);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeStatement statement : statements) {
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
		return statements.hashCode();
	}
}
