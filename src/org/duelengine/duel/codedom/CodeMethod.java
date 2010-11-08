package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a block of statements, providing
 * core parameters (output, model, index, count)
 */
public class CodeMethod extends CodeMember {

	private Class<?> returnType = Void.class;
	private final List<CodeParameterDeclarationExpression> parameters = new ArrayList<CodeParameterDeclarationExpression>();
	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeMethod() {
	}

	public CodeMethod(Class<?> returnType, String methodName, CodeParameterDeclarationExpression[] parameters, CodeStatement[] statements) {
		super(methodName);
		if (returnType != null) {
			this.returnType = returnType;
		}
		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}
		if (statements != null) {
			this.statements.addAll(Arrays.asList(statements));
		}
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	public List<CodeParameterDeclarationExpression> getParameters() {
		return this.parameters;
	}

	public void addParameter(Class<?> type, String name) {
		this.parameters.add(new CodeParameterDeclarationExpression(type, name));
	}

	public CodeStatementCollection getStatements() {
		return this.statements;
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

		if (!this.statements.equals(that.statements)) {
			return false;
		}

		int length = this.parameters.size();
		if (length != that.parameters.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeParameterDeclarationExpression thisParam = this.parameters.get(i);
			CodeParameterDeclarationExpression thatParam = that.parameters.get(i);
			if (thisParam == null ? thatParam != null : !thisParam.equals(thatParam)) {
				return false;
			}
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = ((super.hashCode() * HASH_PRIME) + this.parameters.hashCode()) * HASH_PRIME + this.statements.hashCode();
		if (this.returnType != null) {
			hash = hash * HASH_PRIME + this.returnType.hashCode();
		}

		return hash;
	}
}
