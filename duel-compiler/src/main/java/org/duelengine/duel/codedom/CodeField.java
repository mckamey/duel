package org.duelengine.duel.codedom;

/**
 * Represents an instance field
 */
public class CodeField extends CodeMember {

	private Class<?> type = Object.class;
	private CodeExpression initExpression;

	public CodeField() {
	}

	public CodeField(AccessModifierType access, Class<?> type, String fieldName) {
		this(access, type, fieldName, null);
	}

	public CodeField(AccessModifierType access, Class<?> type, String fieldName, CodeExpression initExpression) {
		super(access, fieldName);
		if (type != null) {
			this.type = type;
		}
		this.initExpression = initExpression;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> value) {
		type = (value != null) ? value : Object.class;
	}

	public CodeExpression getInitExpression() {
		return initExpression;
	}

	public void setInitExpression(CodeExpression value) {
		initExpression = value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (initExpression != null) {
				initExpression.visit(visitor);
			}
		}
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
		if (type != null) {
			hash = hash * HASH_PRIME + type.hashCode();
		}
		if (initExpression != null) {
			hash = hash * HASH_PRIME + initExpression.hashCode();
		}

		return hash;
	}
}
