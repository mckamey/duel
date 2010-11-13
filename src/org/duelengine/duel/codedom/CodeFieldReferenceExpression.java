package org.duelengine.duel.codedom;

public class CodeFieldReferenceExpression extends CodeExpression {

	private CodeExpression target;
	private String fieldName;
	private Class<?> fieldType = Object.class;

	public CodeFieldReferenceExpression() {
	}

	public CodeFieldReferenceExpression(CodeExpression target, Class<?> fieldType, String fieldName) {
		this.target = target;
		this.fieldType = fieldType;
		this.fieldName = fieldName;
	}

	public CodeFieldReferenceExpression(CodeExpression target, CodeField field) {
		this.target = target;
		if (field != null) {
			this.setFieldType(field.getType());
			this.fieldName = field.getName();
		}
	}

	public void setTarget(CodeExpression target) {
		this.target = target;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String value) {
		this.fieldName = value;
	}

	public Class<?> getFieldType() {
		return this.fieldType;
	}

	public void setFieldType(Class<?> value) {
		this.fieldType = (value == null) ? Object.class : value;
	}

	@Override
	public Class<?> getResultType() {
		return this.fieldType;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeFieldReferenceExpression)) {
			// includes null
			return false;
		}

		CodeFieldReferenceExpression that = (CodeFieldReferenceExpression)arg;
		if (this.target == null ? that.target != null : !this.target.equals(that.target)) {
			return false;
		}
		if (this.fieldName == null ? that.fieldName != null : !this.fieldName.equals(that.fieldName)) {
			return false;
		}
		if (this.fieldType == null ? that.fieldType != null : !this.fieldType.equals(that.fieldType)) {
			return false;
		}
		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.target != null) {
			hash = hash * HASH_PRIME + this.target.hashCode();
		}
		if (this.fieldName != null) {
			hash = hash * HASH_PRIME + this.fieldName.hashCode();
		}
		if (this.fieldType != null) {
			hash = hash * HASH_PRIME + this.fieldType.hashCode();
		}
		return hash;
	}
}
