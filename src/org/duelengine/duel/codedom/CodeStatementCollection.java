package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a sequence of statements
 */
public class CodeStatementCollection extends ArrayList<CodeStatement> implements IdentifierScope {

	private static final long serialVersionUID = 1L;
	private Map<String, String> identMap;
	private int nextID;

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
		return this.add(new CodeExpressionStatement(expression));
	}

	public CodeStatement getFirstStatement() {
		if (this.isEmpty()) {
			return null;
		}

		return this.get(0);
	}

	public CodeStatement getLastStatement() {
		if (this.isEmpty()) {
			return null;
		}

		return this.get(this.size()-1);
	}

	@Override
	public String uniqueIdent(String ident) {
		if (this.identMap == null) {
			this.identMap = new HashMap<String, String>();
		}
		else if (this.identMap.containsKey(ident)) {
			return this.identMap.get(ident);
		}

		String unique = this.nextIdent(ident);
		this.identMap.put(ident, unique);
		return unique;
	}

	@Override
	public String nextIdent(String prefix) {
		// generate a unique var name
		return prefix+(++this.nextID);
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
