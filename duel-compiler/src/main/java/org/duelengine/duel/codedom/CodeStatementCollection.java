package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a sequence of statements
 */
@SuppressWarnings("serial")
public class CodeStatementCollection extends ArrayList<CodeStatement> implements IdentifierScope {

	private final CodeObject owner;
	private Map<String, String> identMap;
	private int nextID;

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
	public boolean isLocalIdent(String ident) {
		return (identMap != null) && identMap.containsKey(ident);
	}

	@Override
	public String uniqueIdent(String ident) {
		if (identMap == null) {
			identMap = new HashMap<String, String>();
		}
		else if (identMap.containsKey(ident)) {
			return identMap.get(ident);
		}

		String unique = nextIdent(ident);
		identMap.put(ident, unique);
		return unique;
	}

	@Override
	public String nextIdent(String prefix) {
		// generate a unique var name
		return prefix+(++nextID);
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
