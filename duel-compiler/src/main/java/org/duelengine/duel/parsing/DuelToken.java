package org.duelengine.duel.parsing;

public class DuelToken {

	private final DuelTokenType type;
	private final String value;
	private final BlockValue block;
	private final int index;
	private final int line;
	private final int column;

	private DuelToken(DuelTokenType type, int index, int line, int column) {
		this.type = type;
		this.value = null;
		this.block = null;

		this.index = index;
		this.line = line;
		this.column = column;
	}

	private DuelToken(DuelTokenType type, String value, int index, int line, int column) {
		this.type = type;
		this.value = value;
		this.block = null;

		this.index = index;
		this.line = line;
		this.column = column;
	}

	private DuelToken(DuelTokenType type, BlockValue value, int index, int line, int column) {
		this.type = type;
		this.value = null;
		this.block = value;

		this.index = index;
		this.line = line;
		this.column = column;
	}

	public DuelTokenType getToken() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public BlockValue getBlock() {
		return block;
	}

	public int getIndex() {
		return index;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(type.toString());
		if (value != null) {
			buffer.append(": ").append(value);
		}
		else if (block != null) {
			buffer.append(": ").append(block);
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof DuelToken)) {
			// includes null
			return false;
		}

		DuelToken that = (DuelToken)arg;
		return
			(type.equals(that.type)) &&
			(value == null ? that.value == null : value.equals(that.value)) &&
			(block == null ? that.block == null : block.equals(that.block));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = type.hashCode();
		if (value != null) {
			hash = hash * HASH_PRIME + value.hashCode();
		}
		if (block != null) {
			hash = hash * HASH_PRIME + block.hashCode();
		}
		return hash;
	}

	/* reusable tokens and helper methods */

	static final DuelToken start = new DuelToken(DuelTokenType.LITERAL, -1, -1, -1);
	static final DuelToken end = new DuelToken(DuelTokenType.END, -1, -1, -1);

	public static DuelToken error(String message) {
		return new DuelToken(DuelTokenType.ERROR, message, -1, -1, -1);
	}

	public static DuelToken error(String message, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ERROR, message, index, line, column);
	}

	public static DuelToken elemBegin(String name) {
		return new DuelToken(DuelTokenType.ELEM_BEGIN, name, -1, -1, -1);
	}

	public static DuelToken elemBegin(String name, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ELEM_BEGIN, name, index, line, column);
	}

	public static DuelToken elemEnd(String name) {
		return new DuelToken(DuelTokenType.ELEM_END, name, -1, -1, -1);
	}

	public static DuelToken elemEnd(String name, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ELEM_END, name, index, line, column);
	}

	public static DuelToken attrName(String name) {
		return new DuelToken(DuelTokenType.ATTR_NAME, name, -1, -1, -1);
	}

	public static DuelToken attrName(String name, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ATTR_NAME, name, index, line, column);
	}

	public static DuelToken attrValue(String value) {
		return new DuelToken(DuelTokenType.ATTR_VALUE, value, -1, -1, -1);
	}

	public static DuelToken attrValue(String value, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ATTR_VALUE, value, index, line, column);
	}

	public static DuelToken attrValue(BlockValue value) {
		return new DuelToken(DuelTokenType.ATTR_VALUE, value, -1, -1, -1);
	}

	public static DuelToken attrValue(BlockValue value, int index, int line, int column) {
		return new DuelToken(DuelTokenType.ATTR_VALUE, value, index, line, column);
	}

	public static DuelToken block(BlockValue value) {
		return new DuelToken(DuelTokenType.BLOCK, value, -1, -1, -1);
	}

	public static DuelToken block(BlockValue value, int index, int line, int column) {
		return new DuelToken(DuelTokenType.BLOCK, value, index, line, column);
	}

	public static DuelToken literal(String value) {
		return new DuelToken(DuelTokenType.LITERAL, value, -1, -1, -1);
	}

	public static DuelToken literal(String value, int index, int line, int column) {
		return new DuelToken(DuelTokenType.LITERAL, value, index, line, column);
	}
}
