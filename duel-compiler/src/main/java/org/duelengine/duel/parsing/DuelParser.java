package org.duelengine.duel.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.duelengine.duel.ast.CALLCommandNode;
import org.duelengine.duel.ast.CodeCommentNode;
import org.duelengine.duel.ast.CommandNode;
import org.duelengine.duel.ast.CommentNode;
import org.duelengine.duel.ast.ContainerNode;
import org.duelengine.duel.ast.DocTypeNode;
import org.duelengine.duel.ast.DuelNode;
import org.duelengine.duel.ast.ElementNode;
import org.duelengine.duel.ast.ExpressionNode;
import org.duelengine.duel.ast.FORCommandNode;
import org.duelengine.duel.ast.IFCommandNode;
import org.duelengine.duel.ast.LiteralNode;
import org.duelengine.duel.ast.MarkupExpressionNode;
import org.duelengine.duel.ast.MetaElementNode;
import org.duelengine.duel.ast.PARTCommandNode;
import org.duelengine.duel.ast.StatementNode;
import org.duelengine.duel.ast.UnknownNode;
import org.duelengine.duel.ast.VIEWCommandNode;
import org.duelengine.duel.ast.XORCommandNode;

/**
 * Processes a token sequence into AST
 */
public class DuelParser {

	private DuelToken next;
	private Iterator<DuelToken> tokens;

	/**
	 * Parses token sequence into AST
	 * @param tokenSequence
	 * @return
	 */
	public List<VIEWCommandNode> parse(DuelToken... tokenSequence)
		throws Exception {

		return parse(tokenSequence != null ? Arrays.asList(tokenSequence).iterator() : null);
	}

	/**
	 * Parses token sequence into AST
	 * @param tokenSequence
	 * @return
	 */
	public List<VIEWCommandNode> parse(Iterable<DuelToken> tokenSequence)
		throws Exception {

		return parse(tokenSequence != null ? tokenSequence.iterator() : null);
	}

	/**
	 * Parses token sequence into AST
	 * @param tokenSequence
	 * @return
	 */
	public List<VIEWCommandNode> parse(Iterator<DuelToken> tokenSequence)
		throws IOException {

		if (tokenSequence == null) {
			throw new NullPointerException("tokenSequence");
		}

		tokens = tokenSequence;
		try {

			ContainerNode document = new ContainerNode(0, 0, 0);
			while (hasNext()) {
				parseNext(document);
			}

			List<VIEWCommandNode> views = new ArrayList<VIEWCommandNode>(1);
			for (DuelNode node : document.getChildren()) {
				if (node instanceof VIEWCommandNode) {
					views.add(scrubView((VIEWCommandNode)node));
					continue;
				}

				if (node instanceof LiteralNode) {
					String text = ((LiteralNode)node).getValue();
					if (text == null || text.trim().isEmpty()) {
						continue;
					}
				}

				// syntax error unless is literal whitespace
				throw new InvalidNodeException("Content must sit within a named view.", node);
			}

			return views;
			
		} finally {
			tokens = null;
			next = null;
		}
	}

	/**
	 * Processes the next node
	 * @param parent
	 * @throws Exception
	 */
	private void parseNext(ContainerNode parent)
		throws IOException {
		switch (next.getToken()) {
			case LITERAL:
				parseLiteral(parent);
				break;

			case ELEM_BEGIN:
				parseElem(parent);
				break;

			case ELEM_END:
				if (parent instanceof ElementNode) {
					ElementNode parentElem = (ElementNode)parent;
					if (parentElem.isAncestorOrSelf(next.getValue())) {
						// pass on up
						return;
					}
				}

				// ignore extraneous close tags
				// consume token
				next = null;
				break;

			case BLOCK:
				parseBlock(parent);
				break;

			case ERROR:
				// TODO: back with interface?
				if (tokens instanceof DuelLexer) {
					throw new InvalidTokenException("Syntax error: "+next.getValue(), next, ((DuelLexer)tokens).getLastError());
				}

				throw new InvalidTokenException("Syntax error: "+next.getValue(), next);

			default:
				throw new InvalidTokenException("Invalid token: "+next, next);
		}
	}

	/**
	 * Parses the next token into a literal text node
	 * @param parent
	 */
	private void parseLiteral(ContainerNode parent) {
		DuelNode last = parent.getLastChild();

		if (last instanceof LiteralNode) {
			LiteralNode lastLit = ((LiteralNode)last);

			// perform constant folding of literal strings
			lastLit.setValue(lastLit.getValue() + next.getValue());
		} else {
			// add directly to output
			parent.appendChild(new LiteralNode(next.getValue(), next.getIndex(), next.getLine(), next.getColumn()));
		}

		// consume token
		next = null;
	}

	/**
	 * Parses the next sequence of tokens into a new element node
	 * @param parent
	 */
	private void parseElem(ContainerNode parent)
		throws IOException {

		String tagName = next.getValue();
		ElementNode elem = createElement(tagName, next.getIndex(), next.getLine(), next.getColumn());
		parent.appendChild(elem);

		// consume token
		next = null;
		String attrName = null;

		while (hasNext()) {
			switch (next.getToken()) {
				case ATTR_NAME:
					attrName = next.getValue();
					// set just in case no value
					elem.setAttribute(attrName, null);

					// consume token
					next = null;
					break;

				case ATTR_VALUE:
					if (attrName == null) {
						throw new InvalidTokenException("Attribute name was missing", next);
					}

					BlockValue block = next.getBlock();
					DuelNode attrVal;
					if (block != null) {
						attrVal = createBlock(block, next.getIndex(), next.getLine(), next.getColumn());
					} else {
						attrVal = new LiteralNode(next.getValue(), next.getIndex(), next.getLine(), next.getColumn());
					}
					elem.setAttribute(attrName, attrVal);
					attrName = null;

					// consume token
					next = null;
					break;

				case ELEM_END:
					String tag = next.getValue();
					if (tag != null) {
						tag = tag.toLowerCase();
						if (elem.isSelf(tag)) {
							// consume token
							next = null;

							rewriteConditionalAttr(elem);
							return;
						}
						if (elem.isAncestor(tag)) {
							rewriteConditionalAttr(elem);

							// pass next on up
							return;
						}
					}

					// ignore extraneous close tags
					// consume token
					next = null;
					break;

				default:
					if (!elem.canHaveChildren()) {
						rewriteConditionalAttr(elem);

						// pass next on up
						return;
					}
					parseNext(elem);
					break;
			}
		}
	}

	/**
	 * Parses the next token into a block node
	 * @param parent
	 */
	private void parseBlock(ContainerNode parent) {
		BlockValue block = next.getBlock();

		if (block != null) {
			DuelNode node = createBlock(block, next.getIndex(), next.getLine(), next.getColumn());
			if (node != null) {
				parent.appendChild(node);
			}
		}

		// consume token
		next = null;
	}

	private void rewriteConditionalAttr(ElementNode elem) {
		if (elem instanceof CommandNode && !(elem instanceof CALLCommandNode) && !(elem instanceof FORCommandNode)) {
			// only process normal CALL, FOR, and HTML elements
			return;
		}

		DuelNode attr = elem.removeAttribute(IFCommandNode.IF_ATTR);
		if (attr == null) {
			// nothing to do
			return;
		}

		// create a conditional wrapper and
		// move attr over to the conditional
		IFCommandNode conditional = new IFCommandNode(attr.getIndex(), attr.getLine(), attr.getColumn());
		conditional.setAttribute(IFCommandNode.TEST, attr);

		// use the actual parent node rather than the source parent
		ContainerNode parent = elem.getParent();

		// wrap element in parent
		if (!parent.replaceChild(conditional, elem)) {
			throw new IllegalStateException("Conditional rewrite failed inside "+parent.getClass().getSimpleName());
		}
		conditional.appendChild(elem);
	}

	private VIEWCommandNode scrubView(VIEWCommandNode node) {
		if (node.getName() == null || node.getName().isEmpty()) {
			// syntax error
			throw new InvalidNodeException("View is missing name attribute", node);
		}

		// remove leading and trailing pure whitespace nodes
		
		DuelNode child = node.getLastChild();
		if (child instanceof LiteralNode) {
			String text = ((LiteralNode)child).getValue();
			if (CharUtility.isNullOrWhiteSpace(text)) {
				node.removeChild(child);
			}
		}

		child = node.getFirstChild();
		if (child instanceof LiteralNode) {
			String text = ((LiteralNode)child).getValue();
			if (CharUtility.isNullOrWhiteSpace(text)) {
				node.removeChild(child);
			}
		}
		
		return node;
	}

	/**
	 * Ensures the next node is ready
	 * @return
	 */
	private boolean hasNext() {
		// ensure non-null value
		while (next == null && tokens.hasNext()) {
			next = tokens.next();
		}

		return (next != null);
	}

	/**
	 * ElementNode factory method
	 * @param tagName
	 * @param index
	 * @param line
	 * @param column
	 * @return
	 */
	public static ElementNode createElement(String tagName, int index, int line, int column) {

		if (tagName == null) {
			return null;
		}

		if (tagName.equalsIgnoreCase(FORCommandNode.EXT_NAME)) {
			return new FORCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(XORCommandNode.EXT_NAME)) {
			return new XORCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(IFCommandNode.EXT_NAME)) {
			return new IFCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(CALLCommandNode.EXT_NAME)) {
			return new CALLCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(PARTCommandNode.EXT_NAME)) {
			return new PARTCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(VIEWCommandNode.EXT_NAME)) {
			return new VIEWCommandNode(index, line, column);
		}

		if (tagName.equalsIgnoreCase(MetaElementNode.NAME)) {
			return new MetaElementNode(index, line, column);
		}

		return new ElementNode(tagName.toLowerCase(), index, line, column);
	}

	/**
	 * BlockNode factory method
	 * @param block
	 * @return
	 */
	private DuelNode createBlock(BlockValue block, int index, int line, int column) {

		String begin = block.getBegin();
		if (begin == null) {
			return null;
		}

		String value = block.getValue();

		if (begin.equals(ExpressionNode.BEGIN)) {
			return new ExpressionNode(value, index, line, column);
		}

		if (begin.equals(StatementNode.BEGIN)) {
			return new StatementNode(value, index, line, column);
		}

		if (begin.equals(MarkupExpressionNode.BEGIN)) {
			return new MarkupExpressionNode(value, index, line, column);
		}

		if (begin.equalsIgnoreCase(DocTypeNode.BEGIN)) {
			return new DocTypeNode(value, index, line, column);
		}

		if (begin.equalsIgnoreCase(CommentNode.BEGIN)) {
			return new CommentNode(value, index, line, column);
		}

		if (begin.equalsIgnoreCase(CodeCommentNode.BEGIN)) {
			return new CodeCommentNode(value, index, line, column);
		}

		// others are emitted directly
		return new UnknownNode(begin+value+block.getEnd(), column, column, column);
	}
}
