package org.duelengine.duel.parsing;

import java.util.*;
import org.duelengine.duel.ast.*;

/**
 * Processes a token sequence into AST
 */
public class DuelParser {

	private DuelToken next;
	private Iterator<DuelToken> tokens;

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public List<VIEWCommandNode> parse(DuelToken... tokens)
		throws Exception {

		return this.parse(tokens != null ? Arrays.asList(tokens).iterator() : null);
	}

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public List<VIEWCommandNode> parse(Iterable<DuelToken> tokens)
		throws Exception {

		return this.parse(tokens != null ? tokens.iterator() : null);
	}

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public List<VIEWCommandNode> parse(Iterator<DuelToken> tokens)
		throws Exception {

		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		this.tokens = tokens;
		try {

			ContainerNode document = new ContainerNode(0, 0, 0);
			while (this.hasNext()) {
				this.parseNext(document);
			}

			List<VIEWCommandNode> views = new ArrayList<VIEWCommandNode>(1);
			for (DuelNode node : document.getChildren()) {
				if (node instanceof VIEWCommandNode) {
					views.add(scrubView((VIEWCommandNode)node));
					continue;
				}

				if (node instanceof LiteralNode) {
					String text = ((LiteralNode)node).getValue();
					if (text == null || text.trim().length() == 0) {
						continue;
					}
				}

				// syntax error unless is literal whitespace
				throw new InvalidNodeException("Content must sit within a named view.", node);
			}

			return views;
			
		} finally {
			this.tokens = null;
			this.next = null;
		}
	}

	/**
	 * Processes the next node
	 * @param parent
	 * @throws Exception
	 */
	private void parseNext(ContainerNode parent)
		throws Exception {
		switch (this.next.getToken()) {
			case LITERAL:
				this.parseLiteral(parent);
				break;

			case ELEM_BEGIN:
				this.parseElem(parent);
				break;

			case ELEM_END:
				if (parent instanceof ElementNode) {
					ElementNode parentElem = (ElementNode)parent;
					if (parentElem.isAncestorOrSelf(this.next.getValue())) {
						// pass on up
						return;
					}
				}

				// ignore extraneous close tags
				// consume next
				this.next = null;
				break;

			case BLOCK:
				this.parseBlock(parent);
				break;

			case ERROR:
				// TODO: back with interface?
				if (this.tokens instanceof DuelLexer) {
					throw new InvalidTokenException("Syntax error: "+this.next.getValue(), this.next, ((DuelLexer)this.tokens).getLastError());
				}

				throw new InvalidTokenException("Syntax error: "+this.next.getValue(), this.next);

			default:
				throw new InvalidTokenException("Invalid token: "+this.next, this.next);
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
			lastLit.setValue(lastLit.getValue() + this.next.getValue());
		} else {
			// add directly to output
			parent.appendChild(new LiteralNode(this.next.getValue(), this.next.getIndex(), this.next.getLine(), this.next.getColumn()));
		}

		// consume next
		this.next = null;
	}

	/**
	 * Parses the next sequence of tokens into a new element node
	 * @param parent
	 */
	private void parseElem(ContainerNode parent)
		throws Exception {

		String tagName = this.next.getValue();
		ElementNode elem = createElement(tagName, this.next.getIndex(), this.next.getLine(), this.next.getColumn());
		parent.appendChild(elem);

		// consume next
		this.next = null;
		String attrName = null;

		while (this.hasNext()) {
			switch (this.next.getToken()) {
				case ATTR_NAME:
					attrName = this.next.getValue();
					// set just in case no value
					elem.setAttribute(attrName, null);

					// consume next
					this.next = null;
					break;

				case ATTR_VALUE:
					if (attrName == null) {
						throw new InvalidTokenException("Attribute name was missing", this.next);
					}

					BlockValue block = this.next.getBlock();
					DuelNode attrVal;
					if (block != null) {
						attrVal = this.createBlock(block, this.next.getIndex(), this.next.getLine(), this.next.getColumn());
					} else {
						attrVal = new LiteralNode(this.next.getValue(), this.next.getIndex(), this.next.getLine(), this.next.getColumn());
					}
					elem.setAttribute(attrName, attrVal);
					attrName = null;

					// consume next
					this.next = null;
					break;

				case ELEM_END:
					String tag = this.next.getValue();
					if (tag != null) {
						tag = tag.toLowerCase();
						if (elem.isSelf(tag)) {
							// consume next
							this.next = null;

							this.rewriteConditionalAttr(parent, elem);
							return;
						}
						if (elem.isAncestor(tag)) {
							this.rewriteConditionalAttr(parent, elem);

							// pass next on up
							return;
						}
					}

					// ignore extraneous close tags
					// consume next
					this.next = null;
					break;

				default:
					if (!elem.canHaveChildren()) {
						this.rewriteConditionalAttr(parent, elem);

						// pass next on up
						return;
					}
					this.parseNext(elem);
					break;
			}
		}
	}

	/**
	 * Parses the next token into a block node
	 * @param parent
	 */
	private void parseBlock(ContainerNode parent) {
		BlockValue block = this.next.getBlock();

		if (block != null) {
			DuelNode node = this.createBlock(block, this.next.getIndex(), this.next.getLine(), this.next.getColumn());
			if (node != null) {
				parent.appendChild(node);
			}
		}

		// consume next
		this.next = null;
	}

	private void rewriteConditionalAttr(ContainerNode parent, ElementNode elem) {
		if (elem instanceof CommandNode) {
			// only process normal HTML elements
			return;
		}

		DuelNode attr = elem.removeAttribute("if");
		if (attr == null) {
			// nothing to do
			return;
		}

		// create a conditional wrapper and
		// move attr over to the conditional
		IFCommandNode conditional = new IFCommandNode(attr.getIndex(), attr.getLine(), attr.getColumn());
		conditional.setAttribute("test", attr);

		// wrap element in parent
		parent.replaceChild(conditional, elem);
		conditional.appendChild(elem);
	}

	private VIEWCommandNode scrubView(VIEWCommandNode node) {
		if (node.getName() == null || node.getName().length() == 0) {
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
		while (this.next == null && this.tokens.hasNext()) {
			this.next = this.tokens.next();
		}

		return (this.next != null);
	}

	/**
	 * ElementNode factory method
	 * @param name
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
			return new DocTypeNode(block.getValue(), index, line, column);
		}

		if (begin.equalsIgnoreCase(CommentNode.BEGIN)) {
			return new CommentNode(block.getValue(), index, line, column);
		}

		if (begin.equalsIgnoreCase(CodeCommentNode.BEGIN)) {
			return new CodeCommentNode(block.getValue(), index, line, column);
		}

		// others are dropped
		return null;
	}
}
