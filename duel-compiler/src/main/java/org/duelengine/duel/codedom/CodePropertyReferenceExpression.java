package org.duelengine.duel.codedom;

public class CodePropertyReferenceExpression extends CodeExpression {

	private CodeExpression target;
	private CodeExpression propertyName;

	public CodePropertyReferenceExpression() {
	}

	public CodePropertyReferenceExpression(CodeExpression target, CodeExpression propertyName) {
		this.target = target;
		this.propertyName = propertyName;
	}

	public void setTarget(CodeExpression value) {
		target = value;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public CodeExpression getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(CodeExpression value) {
		propertyName = value;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (target != null) {
				target.visit(visitor);
			}
			if (propertyName != null) {
				propertyName.visit(visitor);
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodePropertyReferenceExpression)) {
			// includes null
			return false;
		}

		CodePropertyReferenceExpression that = (CodePropertyReferenceExpression)arg;
		if (this.target == null ? that.target != null : !this.target.equals(that.target)) {
			return false;
		}
		if (this.propertyName == null ? that.propertyName != null : !this.propertyName.equals(that.propertyName)) {
			return false;
		}
		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (target != null) {
			hash = hash * HASH_PRIME + target.hashCode();
		}
		if (propertyName != null) {
			hash = hash * HASH_PRIME + propertyName.hashCode();
		}
		return hash;
	}
}
