package org.duelengine.duel.codedom;

public class CodeThisReferenceExpression extends CodeExpression {

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeThisReferenceExpression)) {
			// includes null
			return false;
		}
		return super.equals(arg);
	}
}
