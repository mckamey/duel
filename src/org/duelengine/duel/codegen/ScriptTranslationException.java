package org.duelengine.duel.codegen;

import org.duelengine.duel.ast.CodeBlockNode;
import org.duelengine.duel.parsing.InvalidNodeException;
import org.duelengine.duel.parsing.SyntaxException;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ast.AstNode;

@SuppressWarnings("serial")
public class ScriptTranslationException extends SyntaxException {

	private final AstNode node;

	public ScriptTranslationException(String message, AstNode node) {
		super(
			message,
			(node != null) ? node.getAbsolutePosition() : 0,
			(node != null) ? node.getLineno() : 0,
			0);

		this.node = node;
	}

	public ScriptTranslationException(String message, AstNode node, Throwable cause) {
		super(
			message,
			(node != null) ? node.getAbsolutePosition() : 0,
			(node != null) ? node.getLineno() : 0,
			0);

		this.node = node;
	}

	public ScriptTranslationException(String message, EvaluatorException error) {
		super(
			message,
			0,
			(error != null) ? error.lineNumber() : 0,
			(error != null) ? error.columnNumber() : 0,
			error);

		this.node = null;
	}

	public AstNode getNode() {
		return this.node;
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

			int index = this.getIndex() > start ? this.getIndex()-start : 0;
			int line = this.getLine() > 0 ? this.getLine()-1 : 0;
			int column = this.getColumn() > start ? this.getColumn()-start : index;

			return new InvalidNodeException(
				this.getMessage(),
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
