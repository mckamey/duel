package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a sequence of statements
 */
public class CodeStatementCollection implements Iterable<CodeStatement>, IdentifierScope {

	private int nextID;
	private final List<CodeStatement> statements = new ArrayList<CodeStatement>();

	public CodeStatementCollection() {
	}

	public CodeStatementCollection(CodeStatement[] statements) {
		this(Arrays.asList(statements));
	}

	public CodeStatementCollection(Iterable<CodeStatement> statements) {
		if (statements != null) {
			for (CodeStatement statement : statements) {
				this.add(statement);
			}
		}
	}

	public List<CodeStatement> getStatements() {
		return this.statements;
	}

	public void addAll(Iterable<CodeStatement> statements) {
		if (statements != null) {
			for (CodeStatement statement : statements) {
				this.add(statement);
			}
		}
	}

	public void add(CodeExpression expression) {
		this.add(new CodeExpressionStatement(expression));
	}

	public void add(CodeStatement statement) {
		this.statements.add(statement);
	}

	public CodeStatement getLastStatement() {
		if (this.statements.isEmpty()) {
			return null;
		}

		return this.statements.get(this.statements.size()-1);
	}

	public int size() {
		return this.statements.size();
	}

	@Override
	public String nextID(String prefix) {
		// generate a unique var name
		return prefix+(++this.nextID);
	}

	@Override
	public Iterator<CodeStatement> iterator() {
		return this.statements.iterator();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeStatementCollection)) {
			// includes null
			return false;
		}

		CodeStatementCollection that = (CodeStatementCollection)arg;

		int length = this.statements.size();
		if (length != that.statements.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeStatement thisStatement = this.statements.get(i);
			CodeStatement thatStatement = that.statements.get(i);
			if (thisStatement == null ? thatStatement != null : !thisStatement.equals(thatStatement)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return this.statements.hashCode();
	}
}
