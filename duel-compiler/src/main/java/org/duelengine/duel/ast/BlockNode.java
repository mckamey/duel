package org.duelengine.duel.ast;

public abstract class BlockNode extends DuelNode {

	private final String begin;
	private final String end;
	private String value;

	protected BlockNode(String begin, String end, String value, int index, int line, int column) {
		super(index, line, column);
		
		this.begin = begin;
		this.end = end;
		this.value = value;
	}

	protected BlockNode(String begin, String end, String value) {
		this.begin = begin;
		this.end = end;
		this.value = value;
	}

	public String getBegin() {
		return begin;
	}

	public String getEnd() {
		return end;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean hasValue() {
		return (value != null) && !value.isEmpty();
	}

	StringBuilder toString(StringBuilder buffer) {
		if (begin != null) {
			buffer.append(begin);
		}
		if (value != null) {
			buffer.append(value);
		}
		if (end != null) {
			buffer.append(end);
		}

		return buffer;
	}

	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof BlockNode)) {
			// includes null
			return false;
		}

		BlockNode that = (BlockNode)arg;
		return
			(this.begin == null ? that.begin == null : this.begin.equals(that.begin)) &&
			(this.end == null ? that.end == null : this.end.equals(that.end)) &&
			(this.value == null ? that.value == null : this.value.equals(that.value));
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = 0;
		if (begin != null) {
			hash = hash * HASH_PRIME + begin.hashCode();
		}
		if (end != null) {
			hash = hash * HASH_PRIME + end.hashCode();
		}
		if (value != null) {
			hash = hash * HASH_PRIME + value.hashCode();
		}
		return hash;
	}
}
