package org.duelengine.duel.ast;

public class ExpressionNode extends CodeBlockNode {

	public static final String BEGIN = "<%=";
	public static final String END = "%>";
	private static final String INTRO1 = "function(";
	private static final String INTRO2 = "){return(";
	private static final String INTRO2_PRETTY = ") { return (";
	private static final String OUTRO = ");}";
	private static final String OUTRO_PRETTY = "); }";

	public ExpressionNode(String expression, int index, int line, int column) {
		super(BEGIN, END, expression, index, line, column);
	}

	public ExpressionNode(String expression) {
		super(BEGIN, END, expression);
	}

	@Override
	public String getClientCode(boolean prettyPrint) {
		StringBuilder buffer = new StringBuilder(INTRO1);
		buffer.append(formatParamList());
		buffer.append(prettyPrint ? INTRO2_PRETTY : INTRO2);
		if (hasValue()) {
			String value = getValue();
			buffer.append(prettyPrint ? value : value.trim());
		}
		return buffer.append(prettyPrint ? OUTRO_PRETTY : OUTRO).toString();
	}
}
