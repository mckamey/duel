package org.duelengine.duel.codedom;

/**
 * Represents a method call
 */
@SuppressWarnings("rawtypes")
public class CodeVariableDeclarationStatement extends CodeExpression {

	private Class type = Object.class;
	private String name;
	private CodeExpression initExpression;

	public CodeVariableDeclarationStatement() {
	}

	public CodeVariableDeclarationStatement(Class type, String name, CodeExpression initExpression) {
		this.type = type;
		this.name = name;
		this.initExpression = initExpression;
	}

	public Class getType() {
		return this.type;
	}

	public void setType(Class value) {
		this.type = (value == null) ? Object.class : value;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public CodeExpression getInitExpression() {
		return this.initExpression;
	}

	public void setInitExpression(CodeExpression value) {
		this.initExpression = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableDeclarationStatement)) {
			// includes null
			return false;
		}

		CodeVariableDeclarationStatement that = (CodeVariableDeclarationStatement)arg;

		if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
			return false;
		}

		return this.type.equals(that.type);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.type.hashCode();
		if (this.name != null) {
			hash = hash * HASH_PRIME + this.name.hashCode();
		}
		return hash;
	}
}
