package org.duelengine.duel.codedom;

public class CodeVariableReferenceExpression extends CodeExpression {

	private String ident;

	public CodeVariableReferenceExpression() {
	}

	public CodeVariableReferenceExpression(String ident) {
		this.ident = ident;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String value) {
		this.ident = value;
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
