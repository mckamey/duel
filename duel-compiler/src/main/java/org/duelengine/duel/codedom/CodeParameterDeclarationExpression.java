package org.duelengine.duel.codedom;

/**
 * Represents a method call
 */
public class CodeParameterDeclarationExpression extends CodeExpression {

	private String name;
	private Class<?> type = Object.class;
	private boolean varArgs;

	public CodeParameterDeclarationExpression() {
	}

	public CodeParameterDeclarationExpression(Class<?> type, String name) {
		this(type, name, false);
	}

	public CodeParameterDeclarationExpression(Class<?> type, String name, boolean varArgs) {
		this.type = (type == null) ? Object.class : type;
		this.name = name;
		this.varArgs = varArgs;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> value) {
		type = (value == null) ? Object.class : value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public boolean isVarArgs() {
		return varArgs;
	}

	public void setVarArgs(boolean value) {
		varArgs = value;
	}

	@Override
	public Class<?> getResultType() {
		return type;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeParameterDeclarationExpression)) {
			// includes null
			return false;
		}

		CodeParameterDeclarationExpression that = (CodeParameterDeclarationExpression)arg;

		if (name == null ? that.name != null : !name.equals(that.name)) {
			return false;
		}

		return type.equals(that.type) && super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode() * HASH_PRIME + type.hashCode();
		if (name != null) {
			hash = hash * HASH_PRIME + name.hashCode();
		}
		return hash;
	}
}
