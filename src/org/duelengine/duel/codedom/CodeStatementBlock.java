package org.duelengine.duel.codedom;

/**
 * Only used internally to pass around a sequence of statements as a CodeObject
 */
public class CodeStatementBlock extends CodeObject {

	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeStatementBlock() {
	}

	public CodeStatementBlock(CodeStatement[] statements) {
		this.statements.addAll(statements);
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
