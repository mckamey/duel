package org.duelengine.duel.ast;

public enum CommandName {

	/**
	 * No command
	 */
	NONE,

	/**
	 * Defines view metadata
	 */
	VIEW,

	/**
	 * Produces an iterator block
	 */
	FOR,

	/**
	 * Wraps a set of mutually exclusive conditional blocks
	 */
	XOR,

	/**
	 * Represents single or default conditional blocks
	 */
	IF,

	/**
	 * Calls into another view
	 */
	CALL,

	/**
	 * Allows a view to define templated parts of another
	 */
	PART
}
