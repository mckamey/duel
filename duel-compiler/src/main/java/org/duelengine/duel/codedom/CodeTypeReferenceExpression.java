package org.duelengine.duel.codedom;

public class CodeTypeReferenceExpression extends CodeExpression {

	private Class<?> resultType;

	public CodeTypeReferenceExpression() {
		setResultType(null);
	}

	public CodeTypeReferenceExpression(Class<?> type) {
		setResultType(type);
	}

	public void setResultType(Class<?> value) {
		resultType = (value == null) ? Object.class : value;
	}
	
	@Override
	public Class<?> getResultType() {
		return resultType;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeTypeReferenceExpression)) {
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
		if (resultType != null) {
			hash = hash * HASH_PRIME + resultType.hashCode();
		}
		return hash;
	}
}
