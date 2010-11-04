package org.duelengine.duel.codedom;

/**
 * A simplified primitive expression which can only hold a String literal
 */
public class CodePrimitiveExpression extends CodeExpression {

	private String value;

	public CodePrimitiveExpression() {
	}

	public CodePrimitiveExpression(String value) {
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
		if (!(arg instanceof CodePrimitiveExpression)) {
			// includes null
			return false;
		}

		CodePrimitiveExpression that = (CodePrimitiveExpression)arg;
		return (this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		return (this.value == null) ? 0 : this.value.hashCode();
	}
}
