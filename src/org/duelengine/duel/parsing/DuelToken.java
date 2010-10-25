package org.duelengine.duel.parsing;

public class DuelToken {

	private final DuelTokenType type;
	private final String value;
	private final UnparsedBlock unparsed;

	private DuelToken(DuelTokenType type) {
		this.type = type;
		this.value = null;
		this.unparsed = null;
	}

	private DuelToken(DuelTokenType type, String value) {
		this.type = type;
		this.value = value;
		this.unparsed = null;
	}

	private DuelToken(DuelTokenType type, UnparsedBlock value) {
		this.type = type;
		this.value = null;
		this.unparsed = value;
	}

	public DuelTokenType getToken() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	public UnparsedBlock getUnparsed() {
		return this.unparsed;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(this.type.toString());
		if (this.value != null) {
			buffer.append(": "+this.value);
		}
		else if (this.unparsed != null) {
			buffer.append(": "+this.unparsed);
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
			(this.unparsed == null ? that.unparsed == null : this.unparsed.equals(that.unparsed));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.type.hashCode();
		if (this.value != null) {
			hash = hash * HASH_PRIME + this.value.hashCode();
		}
		if (this.unparsed != null) {
			hash = hash * HASH_PRIME + this.unparsed.hashCode();
		}
		return hash;
	}

	/* reusable tokens and helper methods */

	static final DuelToken None = new DuelToken(DuelTokenType.NONE);
	static final DuelToken End = new DuelToken(DuelTokenType.END);

	public static DuelToken Error(String message) {
		return new DuelToken(DuelTokenType.ERROR, message);
	}

	public static DuelToken ElemBegin(String name) {
		return new DuelToken(DuelTokenType.ELEM_BEGIN, name);
	}

	public static DuelToken ElemEnd(String name) {
		return new DuelToken(DuelTokenType.ELEM_END, name);
	}

	public static DuelToken AttrName(String name) {
		return new DuelToken(DuelTokenType.ATTR_NAME, name);
	}

	public static DuelToken AttrValue(String value) {
		return new DuelToken(DuelTokenType.ATTR_LITERAL, value);
	}

	public static DuelToken AttrValue(UnparsedBlock value) {
		return new DuelToken(DuelTokenType.ATTR_UNPARSED, value);
	}

	public static DuelToken Unparsed(UnparsedBlock value) {
		return new DuelToken(DuelTokenType.UNPARSED, value);
	}

	public static DuelToken Literal(String value) {
		return new DuelToken(DuelTokenType.LITERAL, value);
	}
}
