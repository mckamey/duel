package org.duelengine.duel.codedom;

/**
 * A read-only primitive expression which may only hold a literal value
 */
public class CodePrimitiveExpression extends CodeExpression {

	public static final CodePrimitiveExpression NULL = new CodePrimitiveExpression(null);
	public static final CodePrimitiveExpression FALSE = new CodePrimitiveExpression(false);
	public static final CodePrimitiveExpression TRUE = new CodePrimitiveExpression(true);
	public static final CodePrimitiveExpression ZERO = new CodePrimitiveExpression(0);
	public static final CodePrimitiveExpression ONE = new CodePrimitiveExpression(1);

	private final Object value;

	public CodePrimitiveExpression(Object value) {
		Class<?> type = (value == null) ? null : value.getClass();
		if (type != null &&
			!type.isPrimitive() &&
			!Boolean.class.equals(type) &&
			!String.class.equals(type) &&
			!Character.class.equals(type) &&
			!Number.class.isAssignableFrom(type)) {

			throw new IllegalArgumentException("Invalid primitive value: "+type.getName());
		}

		this.value = value;
	}

	public Object getValue() {
		return this.value;
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
