package org.duelengine.duel.parsing;

public class DuelToken {

	private final DuelTokenType type;
	private final String value;
	private final BlockValue block;

	private DuelToken(DuelTokenType type) {
		this.type = type;
		this.value = null;
		this.block = null;
	}

	private DuelToken(DuelTokenType type, String value) {
		this.type = type;
		this.value = value;
		this.block = null;
	}

	private DuelToken(DuelTokenType type, BlockValue value) {
		this.type = type;
		this.value = null;
		this.block = value;
	}

	public DuelTokenType getToken() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	public BlockValue getBlock() {
		return this.block;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(this.type.toString());
		if (this.value != null) {
			buffer.append(": "+this.value);
		}
		else if (this.block != null) {
			buffer.append(": "+this.block);
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
			(this.type.equals(that.type)) &&
			(this.value == null ? that.value == null : this.value.equals(that.value)) &&
			(this.block == null ? that.block == null : this.block.equals(that.block));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.type.hashCode();
		if (this.value != null) {
			hash = hash * HASH_PRIME + this.value.hashCode();
		}
		if (this.block != null) {
			hash = hash * HASH_PRIME + this.block.hashCode();
		}
		return hash;
	}

	/* reusable tokens and helper methods */

	static final DuelToken start = new DuelToken(DuelTokenType.LITERAL);
	static final DuelToken end = new DuelToken(DuelTokenType.END);

	public static DuelToken error(String message) {
		return new DuelToken(DuelTokenType.ERROR, message);
	}

	public static DuelToken elemBegin(String name) {
		return new DuelToken(DuelTokenType.ELEM_BEGIN, name);
	}

	public static DuelToken elemEnd(String name) {
		return new DuelToken(DuelTokenType.ELEM_END, name);
	}

	public static DuelToken attrName(String name) {
		return new DuelToken(DuelTokenType.ATTR_NAME, name);
	}

	public static DuelToken attrValue(String value) {
		return new DuelToken(DuelTokenType.ATTR_LITERAL, value);
	}

	public static DuelToken attrValue(BlockValue value) {
		return new DuelToken(DuelTokenType.ATTR_BLOCK, value);
	}

	public static DuelToken block(BlockValue value) {
		return new DuelToken(DuelTokenType.BLOCK, value);
	}

	public static DuelToken literal(String value) {
		return new DuelToken(DuelTokenType.LITERAL, value);
	}
}
