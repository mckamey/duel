package org.duelengine.duel.ast;

public enum CommandName {

	/**
	 * No command
	 */
	NONE,

	/**
	 * Command which produces an iterator block
	 */
	FOR,

	/**
	 * Command which wraps a set of mutually exclusive conditional blocks
	 */
	XOR,

	/**
	 * Command which represents single and default conditional blocks
	 */
	IF,

	/**
	 * Command which calls another view
	 */
	CALL,

	/**
	 * Command which allows one view to define parts of another
	 */
	PART
}
