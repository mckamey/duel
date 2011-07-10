package org.duelengine.duel.codedom;

/**
 * Represents an extra variable which is defined outside the model data 
 */
public class ScriptVariableReferenceExpression extends ScriptExpression {

	private String ident;

	public ScriptVariableReferenceExpression() {
	}

	public ScriptVariableReferenceExpression(String ident) {
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
		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ScriptVariableReferenceExpression)) {
			// includes null
			return false;
		}

		ScriptVariableReferenceExpression that = (ScriptVariableReferenceExpression)arg;
		if (this.ident == null ? that.ident != null : !this.ident.equals(that.ident)){
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
		return hash;
	}
}
