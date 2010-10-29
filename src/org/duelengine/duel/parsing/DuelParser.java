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
	public DocumentNode parse(DuelToken[] tokens)
		throws Exception {

		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		return this.parse(Arrays.asList(tokens).iterator());
	}

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public DocumentNode parse(Collection<DuelToken> tokens)
		throws Exception {

		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		return this.parse(tokens.iterator());
	}

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public DocumentNode parse(Iterator<DuelToken> tokens)
		throws Exception {

		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		this.tokens = tokens;
		try {

			DocumentNode docFrag = new DocumentNode();
			while (this.hasNext()) {
				this.parseNext(docFrag);
			}
			return docFrag;

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
				// TODO: back with interface
				if (this.tokens instanceof DuelLexer) {
					throw ((DuelLexer)this.tokens).getLastError();
				}

				throw new Exception(this.next.getValue());

			default:
				// TODO: syntax error
				throw new Exception("Invalid next token: "+this.next);
		}
	}

	/**
	 * Parses the next token into a literal text node
	 * @param parent
	 */
	private void parseLiteral(ContainerNode parent) {
		Node last = parent.getLastChild();

		if (last instanceof LiteralNode) {
			LiteralNode lastLit = ((LiteralNode)last);

			// perform constant folding of literal strings
			lastLit.setValue(lastLit.getValue() + this.next.getValue());
		} else {
			// add directly to output
			parent.appendChild(new LiteralNode(this.next.getValue()));
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
		ElementNode elem = createElement(tagName);
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
						// TODO: syntax error
						continue;
					}

					BlockValue block = this.next.getBlock();
					Node attrVal;
					if (block != null) {
						attrVal = this.createBlock(block);
					} else {
						attrVal = new LiteralNode(this.next.getValue());
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
			Node node = this.createBlock(block);
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

		Node attr = elem.removeAttribute("if");
		if (attr == null) {
			// nothing to do
			return;
		}

		// create a conditional wrapper and
		// move attr over to the conditional
		IFCommandNode conditional = new IFCommandNode();
		conditional.setAttribute("test", attr);

		// wrap element in parent
		parent.replaceChild(conditional, elem);
		conditional.appendChild(elem);
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
	public static ElementNode createElement(String tagName) {

		if (tagName == null) {
			return null;
		}

		if (tagName.equalsIgnoreCase(FORCommandNode.EXT_NAME)) {
			return new FORCommandNode();
		}

		if (tagName.equalsIgnoreCase(XORCommandNode.EXT_NAME)) {
			return new XORCommandNode();
		}

		if (tagName.equalsIgnoreCase(IFCommandNode.EXT_NAME)) {
			return new IFCommandNode();
		}

		if (tagName.equalsIgnoreCase(CALLCommandNode.EXT_NAME)) {
			return new CALLCommandNode();
		}

		if (tagName.equalsIgnoreCase(PARTCommandNode.EXT_NAME)) {
			return new PARTCommandNode();
		}

		return new ElementNode(tagName.toLowerCase());
	}

	/**
	 * BlockNode factory method
	 * @param block
	 * @return
	 */
	private BlockNode createBlock(BlockValue block) {

		String begin = block.getBegin();
		if (begin == null) {
			return null;
		}

		String value = block.getValue();

		if (begin.equals(ExpressionNode.BEGIN)) {
			return new ExpressionNode(value);
		}

		if (begin.equals(StatementNode.BEGIN)) {
			return new StatementNode(value);
		}

		if (begin.equals(MarkupNode.BEGIN)) {
			return new MarkupNode(value);
		}

		if (begin.equals(DeclarationNode.BEGIN)) {
			return new DeclarationNode(value);
		}

		if (begin.equalsIgnoreCase(DocTypeNode.BEGIN)) {
			return new DocTypeNode(block.getValue());
		}

		// others are dropped
		return null;
	}
}
