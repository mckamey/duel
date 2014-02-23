package org.duelengine.duel.codegen;

import org.duelengine.duel.ast.CodeBlockNode;
import org.duelengine.duel.parsing.InvalidNodeException;
import org.duelengine.duel.parsing.SyntaxException;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ast.AstNode;

@SuppressWarnings("serial")
public class ScriptTranslationException extends SyntaxException {

	private final AstNode node;

	public ScriptTranslationException(String message, AstNode astNode) {
		super(
			message,
			(astNode != null) ? astNode.getAbsolutePosition() : 0,
			(astNode != null) ? astNode.getLineno() : 0,
			0);

		node = astNode;
	}

	public ScriptTranslationException(String message, AstNode astNode, Throwable cause) {
		super(
			message,
			(astNode != null) ? astNode.getAbsolutePosition() : 0,
			(astNode != null) ? astNode.getLineno() : 0,
			0);

		node = astNode;
	}

	public ScriptTranslationException(String message, EvaluatorException error) {
		super(
			message,
			0,
			(error != null) ? error.lineNumber() : 0,
			(error != null) ? error.columnNumber() : 0,
			error);

		node = null;
	}

	public AstNode getNode() {
		return node;
	}

	/**
	 * Attempts to adjust the position statistics to match the original source
	 * @param ex
	 * @return
	 */
	public SyntaxException adjustErrorStatistics(CodeBlockNode node) {
		try {
			String clientCode = node.getClientCode();
			int start = clientCode.indexOf(node.getValue()) - node.getBegin().length();

			int index = getIndex() > start ? getIndex()-start : 0;
			int line = getLine() > 0 ? getLine()-1 : 0;
			int column = getColumn() > start ? getColumn()-start : index;

			return new InvalidNodeException(
				getMessage(),
				node.getIndex()+index,
				node.getLine()+line,
				node.getColumn()+column,
				node,
				this);

		} catch (Exception ex2) {
			return this;
		}
	}
}
