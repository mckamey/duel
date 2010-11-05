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

	public void setTarget(CodeExpression target) {
		this.target = target;
	}

	public CodeExpression getTarget() {
		return target;
	}

	public CodeExpression getPropertyName() {
		return this.propertyName;
	}

	public void setPropertyName(CodeExpression value) {
		this.propertyName = value;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodePropertyReferenceExpression)) {
			// includes null
			return false;
		}

		CodePropertyReferenceExpression that = (CodePropertyReferenceExpression)arg;
		if (this.propertyName == null ? that.propertyName != null : !this.propertyName.equals(that.propertyName)) {
			return false;
		}
		if (this.target == null ? that.target != null : !this.target.equals(that.target)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return (this.propertyName == null) ? 0 : this.propertyName.hashCode();
	}
}
