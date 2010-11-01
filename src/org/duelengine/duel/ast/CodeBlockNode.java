package org.duelengine.duel.ast;

public abstract class CodeBlockNode extends BlockNode {

	public enum ArgList {
		NONE,
		MODEL,
		INDEX,
		COUNT
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

		if (value.indexOf("count") >= 0) {
			return ArgList.COUNT;
		}

		if (value.indexOf("index") >= 0) {
			return ArgList.INDEX;
		}

		if (value.indexOf("model") >= 0) {
			return ArgList.MODEL;
		}

		return ArgList.NONE;
	}

	protected String formatParamList() {
		switch (this.getParamList()) {
			case MODEL:
				return "model";
			case INDEX:
				return "model, index";
			case COUNT:
				return "model, index, count";
			default:
				return "";
		}
	}
	
	public abstract String getClientCode();
}
