package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents an instance method
 */
public class CodeMethod extends CodeMember {

	private Class<?> returnType = Void.class;
	private final List<CodeParameterDeclarationExpression> parameters = new ArrayList<CodeParameterDeclarationExpression>();
	private final CodeStatementCollection statements = new CodeStatementCollection();
	private boolean isOverride;

	public CodeMethod() {
	}

	public CodeMethod(AccessModifierType access, Class<?> returnType, String methodName, CodeParameterDeclarationExpression[] parameters, CodeStatement... statements) {
		super(access, methodName);
		if (returnType != null) {
			this.returnType = returnType;
		}
		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}
		if (statements != null) {
			this.statements.addAll(statements);
		}
	}

	public Class<?> getReturnType() {
		return this.returnType;
	}

	public void setReturnType(Class<?> value) {
		this.returnType = (value != null) ? value : Void.class;
	}

	public boolean getOverride() {
		return this.isOverride;
	}

	public void setOverride(boolean value) {
		this.isOverride = value;
	}

	public List<CodeParameterDeclarationExpression> getParameters() {
		return this.parameters;
	}

	public void addParameter(Class<?> type, String name) {
		this.addParameter(new CodeParameterDeclarationExpression(type, name));
	}

	public void addParameter(CodeParameterDeclarationExpression parameter) {
		this.parameters.add(parameter);
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
		if (this.isOverride != that.isOverride) {
			return false;
		}

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
