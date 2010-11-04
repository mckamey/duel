package org.duelengine.duel.codedom;

import java.io.Writer;
import java.util.*;

/**
 * Represents a block of statements, providing
 * core parameters (writer, model, index, count)
 */
@SuppressWarnings("rawtypes")
public class CodeMethod extends CodeObject {

	private Class returnType = Void.class;
	private String methodName;
	private final List<CodeParameterDeclarationExpression> parameters = new ArrayList<CodeParameterDeclarationExpression>();
	private final CodeStatementCollection statements = new CodeStatementCollection();

	public CodeMethod() {
	}

	public CodeMethod(Class returnType, String methodName, CodeParameterDeclarationExpression[] parameters, CodeStatement[] statements) {
		if (returnType != null) {
			this.returnType = returnType;
		}
		this.methodName = methodName;
		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}
		if (statements != null) {
			this.statements.addAll(Arrays.asList(statements));
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

	public List<CodeParameterDeclarationExpression> getParameters() {
		return this.parameters;
	}

	public void addParameter(Class type, String name) {
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

		if (this.methodName == null ? that.methodName != null : !this.methodName.equals(that.methodName)) {
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

		return this.statements.equals(that.statements);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.parameters.hashCode() * HASH_PRIME + this.statements.hashCode();
		if (this.returnType != null) {
			hash = hash * HASH_PRIME + this.returnType.hashCode();
		}
		if (this.methodName != null) {
			hash = hash * HASH_PRIME + this.methodName.hashCode();
		}
		return hash;
	}
}