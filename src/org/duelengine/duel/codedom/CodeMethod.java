package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a block of statements and provides
 * core parameters (writer, model, index, count)
 */
public class CodeMethod extends CodeObject {

	private Class returnType = Void.class;
	private String methodName;
	private final List<CodeStatement> statements = new ArrayList<CodeStatement>();

	public CodeMethod() {
	}

	public CodeMethod(CodeStatement[] statements) {
		this(Arrays.asList(statements));
	}

	public CodeMethod(Iterable<CodeStatement> statements) {
		if (statements != null) {
			for (CodeStatement statement : statements) {
				this.addStatement(statement);
			}
		}
	}
	
	public Class getReturnType() {
		return returnType;
	}

	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String value) {
		this.methodName = value;
	}

	public Iterable<CodeStatement> getStatements() {
		return this.statements;
	}

	public void addStatement(CodeStatement statement) {
		this.statements.add(statement);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeMethod)) {
			// includes null
			return false;
		}

		CodeMethod that = (CodeMethod)arg;
		if (this.returnType == null ? that.returnType != null : !this.returnType.equals(that.returnType)) {
			return false;
		}

		if (this.methodName == null ? that.methodName != null : !this.methodName.equals(that.methodName)) {
			return false;
		}

		if ((this.statements == null || that.statements == null) && (this.statements != that.statements)) {
			return false;
		}

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
		final int HASH_PRIME = 1000003;

		int hash = (this.returnType == null) ? 0 : this.returnType.hashCode();
		if (this.methodName != null) {
			hash = hash * HASH_PRIME + this.methodName.hashCode();
		}
		if (this.statements != null) {
			hash = hash * HASH_PRIME + this.statements.hashCode();
		}
		return hash;
	}
}
