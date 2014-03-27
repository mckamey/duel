package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a sequence of statements
 */
@SuppressWarnings("serial")
public class CodeStatementCollection extends ArrayList<CodeStatement> {

	private final CodeObject owner;

	public CodeStatementCollection(CodeObject owner) {
		this.owner = owner;
	}

	public CodeObject getOwner() {
		return owner;
	}

	public boolean addAll(CodeStatementBlock block) {
		if (block == null) {
			return false;
		}

		for (CodeStatement statement : block.getStatements()) {
			super.add(statement);
		}
		return true;
	}

	public boolean addAll(CodeStatement[] statements) {
		if (statements == null) {
			return false;
		}

		for (CodeStatement statement : statements) {
			super.add(statement);
		}
		return true;
	}

	public boolean add(CodeExpression expression) {
		return add(new CodeExpressionStatement(expression));
	}

	public CodeStatement getFirstStatement() {
		if (isEmpty()) {
			return null;
		}

		return get(0);
	}

	public CodeStatement getLastStatement() {
		if (isEmpty()) {
			return null;
		}

		return get(size()-1);
	}

	@Override
	public Iterator<CodeStatement> iterator() {
		return super.iterator();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeStatementCollection)) {
			// includes null
			return false;
		}

		CodeStatementCollection that = (CodeStatementCollection)arg;

		int length = this.size();
		if (length != that.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeStatement thisStatement = this.get(i);
			CodeStatement thatStatement = that.get(i);
			if (thisStatement == null ? thatStatement != null : !thisStatement.equals(thatStatement)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
