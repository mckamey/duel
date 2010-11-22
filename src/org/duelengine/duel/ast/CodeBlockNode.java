package org.duelengine.duel.ast;

public abstract class CodeBlockNode extends BlockNode {

	public enum ArgList {
		NONE,
		DATA,
		INDEX,
		COUNT,
		KEY
	}

	protected CodeBlockNode(String begin, String end, String value, int index, int line, int column) {
		super(begin, end, value, index, line, column);
	}

	protected CodeBlockNode(String begin, String end, String value) {
		super(begin, end, value);
	}

	public ArgList getParamList() {
		// NOTE: this is pretty basic right now
		// it could result in false positives
		// but it should reduce bulk for most cases
		
		String value = this.getValue();
		if (value == null) {
			return ArgList.NONE;
		}

		if (value.indexOf("key") >= 0) {
			return ArgList.KEY;
		}

		if (value.indexOf("count") >= 0) {
			return ArgList.COUNT;
		}

		if (value.indexOf("index") >= 0) {
			return ArgList.INDEX;
		}

		if (value.indexOf("data") >= 0) {
			return ArgList.DATA;
		}

		return ArgList.NONE;
	}

	protected String formatParamList() {
		switch (this.getParamList()) {
			case DATA:
				return "data";
			case INDEX:
				return "data, index";
			case COUNT:
				return "data, index, count";
			case KEY:
				return "data, index, count, key";
			default:
				return "";
		}
	}
	
	public abstract String getClientCode();
}
