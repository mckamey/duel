package org.duelengine.duel.ast;

public abstract class BlockNode extends Node {

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
		return this.begin;
	}

	public String getEnd() {
		return this.end;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	StringBuilder toString(StringBuilder buffer) {
		if (this.begin != null) {
			buffer.append(this.begin);
		}
		if (this.value != null) {
			buffer.append(this.value);
		}
		if (this.end != null) {
			buffer.append(this.end);
		}

		return buffer;
	}

	@Override
	public String toString() {
		return this.toString(new StringBuilder()).toString();
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
		if (this.begin != null) {
			hash = hash * HASH_PRIME + this.begin.hashCode();
		}
		if (this.end != null) {
			hash = hash * HASH_PRIME + this.end.hashCode();
		}
		if (this.value != null) {
			hash = hash * HASH_PRIME + this.value.hashCode();
		}
		return hash;
	}}
