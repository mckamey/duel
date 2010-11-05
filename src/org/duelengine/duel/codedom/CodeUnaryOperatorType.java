package org.duelengine.duel.codedom;

public enum CodeUnaryOperatorType {

	NONE,

	/**
	 * Positive numeric "+x"
	 */
	POSITIVE,

	/**
	 * Negative numeric "-x"
	 */
	NEGATION,		

	/**
	 * Logical not "!x"
	 */
	LOGICAL_NEGATION,

	/**
	 * Bitwise not "~x"
	 */
	BITWISE_NEGATION,

	/**
	 * INC before "++x"
	 */
	PRE_INCREMENT,

	/**
	 * DEC before "--x"
	 */
	PRE_DECREMENT,

	/**
	 * INC after "x++"
	 */
	POST_INCREMENT,

	/**
	 * DEC after "x--"
	 */
	POST_DECREMENT
}
