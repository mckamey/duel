package org.duelengine.duel.codedom;

public class CodeVariableReferenceExpression extends CodeExpression {

	private String ident;

	public CodeVariableReferenceExpression() {
	}

	public CodeVariableReferenceExpression(String ident) {
		this.ident = ident;
	}

	public String getIdent() {
		return this.ident;
	}

	public void setIdent(String value) {
		this.ident = value;
	}

	@Override
	public Class<?> getResultType() {
		if (this.ident != null && (this.ident.equals("index") || this.ident.equals("count"))) {
			return Integer.class;
		}

		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableReferenceExpression)) {
			// includes null
			return false;
		}

		CodeVariableReferenceExpression that = (CodeVariableReferenceExpression)arg;
		return (this.ident == null ? that.ident == null : this.ident.equals(that.ident));
	}

	@Override
	public int hashCode() {
		return (this.ident == null) ? 0 : this.ident.hashCode();
	}
}
