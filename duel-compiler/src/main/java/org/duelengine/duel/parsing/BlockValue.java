package org.duelengine.duel.parsing;

public class BlockValue {

	private final String begin;
	private final String end;
	private final String value;

	public BlockValue(String beginDelim, String endDelim, String blockValue) {
		begin = beginDelim;
		end = endDelim;
		value = blockValue;
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
		if (begin != null) {
			buffer.append(begin);
		}
		if (value != null) {
			buffer.append(value);
		}
		if (end != null) {
			buffer.append(end);
		}
		return buffer.toString();
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof BlockValue)) {
			// includes null
			return false;
		}

		BlockValue that = (BlockValue)arg;
		return
			(begin == null ? that.begin == null : begin.equals(that.begin)) &&
			(end == null ? that.end == null : end.equals(that.end)) &&
			(value == null ? that.value == null : value.equals(that.value));
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
