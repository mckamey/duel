package org.duelengine.duel.ast;

public class StatementNode extends CodeBlockNode {

	public static final String BEGIN = "<%";
	public static final String END = "%>";
	private static final String INTRO1 = "function(";
	private static final String INTRO2 = "){";
	private static final String INTRO2_PRETTY = ") { ";
	private static final String OUTRO = "}";
	private static final String OUTRO_PRETTY = " }";

	public StatementNode(String statement, int index, int line, int column) {
		super(BEGIN, END, statement, index, line, column);
	}

	public StatementNode(String statement) {
		super(BEGIN, END, statement);
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
