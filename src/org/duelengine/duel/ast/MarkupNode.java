package org.duelengine.duel.ast;

public class MarkupNode extends CodeBlockNode {

	public static final String BEGIN = "<%#";
	public static final String END = "%>";
	private static final String INTRO = "function(model, index, count) { return duel.raw(";
	private static final String OUTRO = "); }";

	public MarkupNode(String expression) {
		super(BEGIN, END, expression);
	}

	@Override
	public String getClientCode() {
		StringBuilder buffer = new StringBuilder(INTRO);
		String value = this.getValue();
		if (value != null) {
			buffer.append(value);
		}
		return buffer.append(OUTRO).toString();
	}
}
