package org.duelengine.duel.codedom;

/**
 * Represents an instance field
 */
public class CodeField extends CodeMember {

	private Class<?> type = Object.class;
	private CodeExpression initExpression;

	public CodeField() {
	}

	public CodeField(AccessModifierType access, Class<?> type, String methodName, CodeExpression initExpression) {
		super(access, methodName);
		if (type != null) {
			this.type = type;
		}
		this.initExpression = initExpression;
	}

	public Class<?> getType() {
		return this.type;
	}

	public void setType(Class<?> value) {
		this.type = (value != null) ? value : Object.class;
	}

	public CodeExpression getInitExpression() {
		return this.initExpression;
	}

	public void setInitExpression(CodeExpression value) {
		this.initExpression = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeField)) {
			// includes null
			return false;
		}

		CodeField that = (CodeField)arg;
		if (this.type == null ? that.type != null : !this.type.equals(that.type)) {
			return false;
		}
		if (this.initExpression == null ? that.initExpression != null : !this.initExpression.equals(that.initExpression)) {
			return false;
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.type != null) {
			hash = hash * HASH_PRIME + this.type.hashCode();
		}
		if (this.initExpression != null) {
			hash = hash * HASH_PRIME + this.initExpression.hashCode();
		}

		return hash;
	}
}
