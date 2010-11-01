package org.duelengine.duel.ast;

public class StatementNode extends CodeBlockNode {

	public static final String BEGIN = "<%";
	public static final String END = "%>";
	private static final String INTRO = "function(model, index, count) { ";
	private static final String OUTRO = " }";

	public StatementNode(String expression) {
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
