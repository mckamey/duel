package org.duelengine.duel.codedom;

public class CodeVariableReferenceExpression extends CodeExpression {

	private String ident;
	private Class<?> resultType = Object.class;

	public CodeVariableReferenceExpression() {
	}

	public CodeVariableReferenceExpression(Class<?> type, String ident) {
		this.ident = ident;
		this.setResultType(type);
	}

	public CodeVariableReferenceExpression(CodeVariableDeclarationStatement variable) {
		if (variable != null) {
			this.ident = variable.getName();
			this.setResultType(variable.getType());
		}
	}

	public CodeVariableReferenceExpression(CodeParameterDeclarationExpression variable) {
		if (variable != null) {
			this.ident = variable.getName();
			this.setResultType(variable.getType());
		}
	}

	public String getIdent() {
		return this.ident;
	}

	public void setIdent(String value) {
		this.ident = value;
	}

	@Override
	public Class<?> getResultType() {
		return this.resultType;
	}

	public void setResultType(Class<?> value) {
		this.resultType = (value == null) ? Object.class : value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableReferenceExpression)) {
			// includes null
			return false;
		}

		CodeVariableReferenceExpression that = (CodeVariableReferenceExpression)arg;
		if (this.ident == null ? that.ident != null : !this.ident.equals(that.ident)) {
			return false;
		}
		if (this.resultType == null ? that.resultType != null : !this.resultType.equals(that.resultType)) {
			return false;
		}
		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.ident != null) {
			hash = hash * HASH_PRIME + this.ident.hashCode();
		}
		if (this.resultType != null) {
			hash = hash * HASH_PRIME + this.resultType.hashCode();
		}
		return hash;
	}
}
