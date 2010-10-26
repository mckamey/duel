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

		return this.parse(Arrays.asList(tokens).iterator());
	}

	/**
	 * Parses token sequence into AST
	 * @param tokens
	 * @return
	 */
	public DocumentNode parse(Collection<DuelToken> tokens)
		throws Exception {

		return this.parse(tokens.iterator());
	}

	/**
	 * Ctor
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
				BlockValue block = this.next.getBlock();

				if (block != null) {
					Node node = this.parseBlock(block);
					if (node != null) {
						parent.appendChild(node);
					}
				}

				// consume next
				this.next = null;
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
		ElementNode elem = new ElementNode((tagName != null) ? tagName.toLowerCase() : null);
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
						attrVal = this.parseBlock(block);
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
							return;
						}
						if (elem.isAncestor(tag)) {
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
						// pass next on up
						return;
					}
					this.parseNext(elem);
					break;
			}
		}
	}

	/**
	 * BlockNode factory
	 * @param block
	 * @return
	 */
	private BlockNode parseBlock(BlockValue block) {
		String begin = block.getBegin();
		if (begin == null) {
			return null;
		}

		if (begin.equals(DocTypeNode.BEGIN)) {
			return new DocTypeNode(block.getValue());
		}

		return null;
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
}
