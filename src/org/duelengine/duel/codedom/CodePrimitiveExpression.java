package org.duelengine.duel.codedom;

/**
 * A simplified primitive expression which can only hold a String literal
 */
public class CodePrimitiveExpression extends CodeExpression {

	public static final CodePrimitiveExpression NULL = new CodePrimitiveExpression(null);
	public static final CodePrimitiveExpression FALSE = new CodePrimitiveExpression(false);
	public static final CodePrimitiveExpression TRUE = new CodePrimitiveExpression(true);
	
	private Object value;

	public CodePrimitiveExpression() {
	}

	public CodePrimitiveExpression(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public Class<?> getResultType() {
		return (this.value == null) ? Object.class : this.value.getClass();
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
