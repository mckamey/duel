package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance method
 */
public class CodeMethod extends CodeMember implements IdentifierScope {

	private Class<?> returnType;
	private final List<CodeParameterDeclarationExpression> parameters = new ArrayList<CodeParameterDeclarationExpression>();
	private final CodeStatementCollection statements;
	private final List<Class<?>> exceptions = new ArrayList<Class<?>>();
	private boolean override;
	private Map<String, String> identMap;
	private int nextID;

	public CodeMethod() {
		this(AccessModifierType.DEFAULT, null, null, null);
	}

	public CodeMethod(AccessModifierType access, Class<?> returnType, String methodName,
		CodeParameterDeclarationExpression[] parameters, CodeStatement... statements) {

		super(access, methodName);

		setReturnType(returnType);

		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}

		this.statements = new CodeStatementCollection(this);
		if (statements != null) {
			this.statements.addAll(statements);
		}
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	public void setReturnType(Class<?> value) {
		returnType = (value != null) ? value : Void.class;
	}

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean value) {
		override = value;
	}

	public CodeMethod withOverride() {
		override = true;
		return this;
	}

	public List<CodeParameterDeclarationExpression> getParameters() {
		return parameters;
	}

	public void addParameter(Class<?> type, String name) {
		addParameter(new CodeParameterDeclarationExpression(type, name));
	}

	public void addParameter(CodeParameterDeclarationExpression parameter) {
		parameters.add(parameter);
	}

	public List<Class<?>> getThrows() {
		return exceptions;
	}

	public void addThrows(Class<?> type) {
		exceptions.add(type);
	}

	public CodeMethod withThrows(Class<?>... types) {
		if (types != null) {
			exceptions.addAll(Arrays.asList(types));
		}
		
		return this;
	}

	public CodeStatementCollection getStatements() {
		return statements;
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
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeParameterDeclarationExpression parameter : parameters) {
				if (parameter != null) {
					parameter.visit(visitor);
				}
			}
			for (CodeStatement statement : statements) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
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

		int hash = (((super.hashCode() * HASH_PRIME) + parameters.hashCode()) * HASH_PRIME + exceptions.hashCode()) * HASH_PRIME + statements.hashCode();
		if (returnType != null) {
			hash = hash * HASH_PRIME + returnType.hashCode();
		}

		return hash;
	}
}
