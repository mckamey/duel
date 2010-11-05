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
		if (this.value == null ? that.value != null : !this.value.equals(that.value)) {
			return false;
		}
		
		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.value != null) {
			hash = hash * HASH_PRIME + this.value.hashCode();
		}
		return hash;
	}
}
