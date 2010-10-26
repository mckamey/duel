package org.duelengine.duel.parsing;

public enum DuelTokenType {

	/**
	 * Literal text
	 */
	LITERAL,

	/**
	 * Element begin tag 
	 */
	ELEM_BEGIN,

	/**
	 * Element end tag
	 */
	ELEM_END,

	/**
	 * Attribute name
	 */
	ATTR_NAME,

	/**
	 * Attribute value
	 */
	ATTR_VALUE,

	/**
	 * Unparsed block
	 */
	BLOCK,

	/**
	 * Error state
	 */
	ERROR,

	/**
	 * End of file
	 */
	END
}
