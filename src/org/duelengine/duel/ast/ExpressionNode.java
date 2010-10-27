package org.duelengine.duel.ast;

public class ExpressionNode extends CodeBlockNode {

	public static final String BEGIN = "<%=";
	public static final String END = "%>";
	private static final String INTRO = "function(model, index, count) { return (";
	private static final String OUTRO = "); }";

	public ExpressionNode(String expression) {
		super(BEGIN, END, expression);
	}

	@Override
	public String getCode() {
		StringBuilder buffer = new StringBuilder(INTRO);
		String value = this.getValue();
		if (value != null) {
			buffer.append(value);
		}
		return buffer.append(OUTRO).toString();
	}
}
