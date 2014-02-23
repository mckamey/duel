package org.duelengine.duel.codedom;

import org.duelengine.duel.JSUtility;

/**
 * A read-only primitive expression which may only hold a literal value
 */
public class CodePrimitiveExpression extends CodeExpression {

	public static final CodePrimitiveExpression UNDEFINED = new CodePrimitiveExpression(JSUtility.UNDEFINED);
	public static final CodePrimitiveExpression NULL = new CodePrimitiveExpression(null);
	public static final CodePrimitiveExpression FALSE = new CodePrimitiveExpression(false);
	public static final CodePrimitiveExpression TRUE = new CodePrimitiveExpression(true);
	public static final CodePrimitiveExpression ZERO = new CodePrimitiveExpression(0);
	public static final CodePrimitiveExpression ONE = new CodePrimitiveExpression(1);

	private final Object value;

	public CodePrimitiveExpression(Object primative) {
		Class<?> type = (primative == null || primative == JSUtility.UNDEFINED) ? null : primative.getClass();
		if (type != null &&
			!type.isPrimitive() &&
			!Boolean.class.equals(type) &&
			!String.class.equals(type) &&
			!Character.class.equals(type) &&
			!Number.class.isAssignableFrom(type)) {

			throw new IllegalArgumentException("Invalid primitive value: "+type.getName());
		}

		value = primative;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public Class<?> getResultType() {
		return (value == null) ? Object.class : value.getClass();
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
		if (value != null) {
			hash = hash * HASH_PRIME + value.hashCode();
		}
		return hash;
	}
}
