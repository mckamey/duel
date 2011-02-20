package org.duelengine.duel.codedom;

public enum AccessModifierType {

	/**
	 * Allows access by any package member
	 */
	DEFAULT,

	/**
	 * Allows access only by declaring class
	 */
	PRIVATE,

	/**
	 * Allows access by any inheriting class
	 */
	PROTECTED,

	/**
	 * Allows access by any class
	 */
	PUBLIC	
}
