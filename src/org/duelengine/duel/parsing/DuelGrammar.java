package org.duelengine.duel.parsing;

final class DuelGrammar {

	// static class
	private DuelGrammar() {}

	public static final int EOF = -1;

	public static final char OP_ELEM_BEGIN = '<';
	public static final char OP_ELEM_END = '>';
	public static final char OP_ELEM_CLOSE = '/';
	public static final char OP_ATTR_DELIM = ' ';
	public static final char OP_PAIR_DELIM = '=';
	public static final char OP_PREFIX_DELIM = ':';

	public static final char OP_STRING_DELIM = '"';
	public static final char OP_STRING_DELIM_ALT = '\'';

	public static final char OP_ENTITY_BEGIN = '&';
	public static final char OP_ENTITY_NUM = '#';
	public static final char OP_ENTITY_HEX = 'x';
	public static final char OP_ENTITY_HEX_ALT = 'X';
	public static final char OP_ENTITY_END = ';';
}
