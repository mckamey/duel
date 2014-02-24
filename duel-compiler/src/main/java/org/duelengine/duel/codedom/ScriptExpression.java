package org.duelengine.duel.codedom;

import org.duelengine.duel.JSUtility;

/**
 * A built-in JavaScript concept which must be interpreted
 */
public abstract class ScriptExpression extends CodeExpression {

	public static final CodeFieldReferenceExpression UNDEFINED = new CodeFieldReferenceExpression(
			new CodeTypeReferenceExpression(JSUtility.class), Object.class, "UNDEFINED");
}
