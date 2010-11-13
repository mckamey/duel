package org.duelengine.duel.codedom;

/**
 * Represents a method call
 */
public class CodeParameterDeclarationExpression extends CodeExpression {

	private String name;
	private Class<?> type = Object.class;
	private boolean isVarArgs;

	public CodeParameterDeclarationExpression() {
	}

	public CodeParameterDeclarationExpression(Class<?> type, String name) {
		this(type, name, false);
	}

	public CodeParameterDeclarationExpression(Class<?> type, String name, boolean isVarArgs) {
		this.type = (type == null) ? Object.class : type;
		this.name = name;
		this.isVarArgs = isVarArgs;
	}

	public Class<?> getType() {
		return this.type;
	}

	public void setType(Class<?> value) {
		this.type = (value == null) ? Object.class : value;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public boolean getVarArgs() {
		return this.isVarArgs;
	}

	public void setVarArgs(boolean value) {
		this.isVarArgs = value;
	}

	@Override
	public Class<?> getResultType() {
		return this.type;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeParameterDeclarationExpression)) {
			// includes null
			return false;
		}

		CodeParameterDeclarationExpression that = (CodeParameterDeclarationExpression)arg;

		if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
			return false;
		}

		return this.type.equals(that.type) && super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode() * HASH_PRIME + this.type.hashCode();
		if (this.name != null) {
			hash = hash * HASH_PRIME + this.name.hashCode();
		}
		return hash;
	}
}
