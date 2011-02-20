package org.duelengine.duel.codedom;

/**
 * Represents a single code statement
 */
public abstract class CodeStatement extends CodeObject {

	@Override
	public CodeStatement withUserData(Object... pairs) {
		return (CodeStatement)super.withUserData(pairs);
	}
}
