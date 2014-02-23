package org.duelengine.duel.codedom;

public class CodeCommentStatement extends CodeStatement {

	private String value;

	public CodeCommentStatement() {
	}

	public CodeCommentStatement(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeCommentStatement)) {
			// includes null
			return false;
		}

		CodeCommentStatement that = (CodeCommentStatement)arg;
		return (this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		return (value == null) ? 0 : value.hashCode();
	}
}
