package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents an instance method
 */
public class CodeMethod extends CodeMember {

	private Class<?> returnType;
	private final List<CodeParameterDeclarationExpression> parameters = new ArrayList<CodeParameterDeclarationExpression>();
	private final CodeStatementCollection statements;
	private final List<Class<?>> exceptions = new ArrayList<Class<?>>();
	private boolean override;

	public CodeMethod() {
		this(AccessModifierType.DEFAULT, null, null, null);
	}

	public CodeMethod(AccessModifierType access, Class<?> returnType, String methodName,
		CodeParameterDeclarationExpression[] parameters, CodeStatement... statements) {

		super(access, methodName);

		this.setReturnType(returnType);

		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}

		this.statements = new CodeStatementCollection(this);
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

	public boolean isOverride() {
		return this.override;
	}

	public void setOverride(boolean value) {
		this.override = value;
	}

	public CodeMethod withOverride() {
		this.override = true;
		return this;
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

	public List<Class<?>> getThrows() {
		return this.exceptions;
	}

	public void addThrows(Class<?> type) {
		this.exceptions.add(type);
	}

	public CodeMethod withThrows(Class<?>... exceptions) {
		if (exceptions != null) {
			this.exceptions.addAll(Arrays.asList(exceptions));
		}
		
		return this;
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
		if (this.override != that.override) {
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

		length = this.exceptions.size();
		if (length != that.exceptions.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			Class<?> thisEx = this.exceptions.get(i);
			Class<?> thatEx = that.exceptions.get(i);
			if (thisEx == null ? thatEx != null : !thisEx.equals(thatEx)) {
				return false;
			}
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (((super.hashCode() * HASH_PRIME) + this.parameters.hashCode()) * HASH_PRIME + this.exceptions.hashCode()) * HASH_PRIME + this.statements.hashCode();
		if (this.returnType != null) {
			hash = hash * HASH_PRIME + this.returnType.hashCode();
		}

		return hash;
	}
}
