package org.duelengine.duel.codedom;

public class CodeTypeReferenceExpression extends CodeExpression {

	private Class<?> resultType;

	public CodeTypeReferenceExpression() {
		this.setResultType(null);
	}

	public CodeTypeReferenceExpression(Class<?> type) {
		this.setResultType(type);
	}

	public void setResultType(Class<?> value) {
		this.resultType = (value == null) ? Object.class : value;
	}
	
	@Override
	public Class<?> getResultType() {
		return this.resultType;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableReferenceExpression)) {
			// includes null
			return false;
		}

		CodeTypeReferenceExpression that = (CodeTypeReferenceExpression)arg;
		if (this.resultType == null ? that.resultType != null : !this.resultType.equals(that.resultType)) {
			return false;
		}
		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.resultType != null) {
			hash = hash * HASH_PRIME + this.resultType.hashCode();
		}
		return hash;
	}
}
