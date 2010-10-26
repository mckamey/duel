package org.duelengine.duel.ast;

public class BlockNode extends Node {

	private String begin;
	private String end;
	private String value;

	public void setBegin(String value) {
		this.begin = value;
	}

	public String getBegin() {
		return this.begin;
	}

	public void setEnd(String value) {
		this.end = value;
	}

	public String getEnd() {
		return this.end;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
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
