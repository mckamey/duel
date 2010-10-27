package org.duelengine.duel.ast;

public enum CommandName {

	/**
	 * No command
	 */
	NONE,

	/**
	 * Element command produces an iterator block
	 */
	FOR,

	/**
	 * Element command wraps a set of mutually exclusive conditional blocks
	 */
	XOR,

	/**
	 * Element command represents single and default conditional blocks
	 */
	IF,

	/**
	 * Element command calls another view
	 */
	CALL
}
