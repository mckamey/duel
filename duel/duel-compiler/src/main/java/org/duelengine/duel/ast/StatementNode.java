package org.duelengine.duel.ast;

public class StatementNode extends CodeBlockNode {

	public static final String BEGIN = "<%";
	public static final String END = "%>";
	private static final String INTRO1 = "function(";
	private static final String INTRO2 = ") { ";
	private static final String OUTRO = " }";

	public StatementNode(String statement, int index, int line, int column) {
		super(BEGIN, END, statement, index, line, column);
	}

	public StatementNode(String statement) {
		super(BEGIN, END, statement);
	}

	@Override
	public String getClientCode() {
		StringBuilder buffer = new StringBuilder(INTRO1);
		buffer.append(this.formatParamList());
		buffer.append(INTRO2);
		String value = this.getValue();
		if (value != null) {
			buffer.append(value);
		}
		return buffer.append(OUTRO).toString();
	}
}
