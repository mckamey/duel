package org.duelengine.duel.codedom;

/**
 * Emits a string into the output
 */
public class CodeEmitLiteralStatement extends CodeStatement {

	private String value;

	public CodeEmitLiteralStatement() {
	}

	public CodeEmitLiteralStatement(String value) {
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
		if (!(arg instanceof CodeEmitLiteralStatement)) {
			// includes null
			return false;
		}

		CodeEmitLiteralStatement that = (CodeEmitLiteralStatement)arg;
		return (this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		return (this.value == null) ? 0 : this.value.hashCode();
	}
}
