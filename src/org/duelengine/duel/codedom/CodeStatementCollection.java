package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a statement block
 */
public class CodeStatementCollection implements Iterable<CodeStatement>, UniqueNameGenerator {

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

	public Iterable<CodeStatement> getStatements() {
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

	@Override
	public String nextID() {
		// generate a unique var name
		return "t_"+(++this.nextID);
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
