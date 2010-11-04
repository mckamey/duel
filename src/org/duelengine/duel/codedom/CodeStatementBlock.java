package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a block of statements
 */
public class CodeStatementBlock extends CodeObject implements Iterable<CodeStatement> {

	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeStatementBlock() {
	}

	public CodeStatementBlock(CodeStatement[] statements) {
		if (statements != null) {
			this.statements.addAll(Arrays.asList(statements));
		}
	}

	public Iterable<CodeStatement> getStatements() {
		return this.statements;
	}

	public void addAll(Iterable<CodeStatement> statements) {
		this.statements.addAll(statements);
	}

	public void add(CodeExpression expression) {
		this.statements.add(expression);
	}

	public void add(CodeStatement statement) {
		this.statements.add(statement);
	}

	@Override
	public Iterator<CodeStatement> iterator() {
		return this.statements.iterator();
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
