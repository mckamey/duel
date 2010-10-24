package org.duelengine.duel.parsing;

public class UnparsedBlock {

	private final String begin;
	private final String end;

	private final String value;

	public UnparsedBlock(String begin, String end, String value) {
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

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (this.begin != null) {
			buffer.append(this.begin);
		}
		if (this.value != null) {
			buffer.append(this.value);
		}
		if (this.end != null) {
			buffer.append(this.end);
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof UnparsedBlock)) {
			// includes null
			return false;
		}

		UnparsedBlock that = (UnparsedBlock)arg;
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
	}
}
