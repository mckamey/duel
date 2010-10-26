package org.duelengine.duel.parsing;

import java.util.*;

import org.duelengine.duel.ast.*;

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

		DocumentNode docFrag = new DocumentNode();

		this.tokens = tokens;
		try {
			while (this.next != null || this.tokens.hasNext()) {
				if (this.next == null) {
					this.next = this.tokens.next();
					if (this.next == null) {
						continue;
					}
				}
				this.parseNext(docFrag);
			}
		} finally {
			this.tokens = null;
		}

		return docFrag;
	}

	private void parseNext(ContainerNode parent)
		throws Exception {
		switch (this.next.getToken()) {
			case LITERAL:
				this.parseLiteral(parent);
				break;

			case ELEM_BEGIN:
				this.parseElem(parent);
				break;

			case BLOCK:
				this.parseBlock(parent);
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

			default:
				// TODO: syntax error
				throw new Exception("Invalid next token: "+this.next);
		}
	}

	private void parseBlock(ContainerNode parent) {
		BlockValue block = this.next.getBlock();

		String begin = block.getBegin();
		if (begin != null) {
			if (begin.equals(DocTypeNode.BEGIN)) {
				parent.appendChild(new DocTypeNode(block.getValue()));
			}
		}
		
		// consume next
		this.next = null;
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

		while (this.next != null || this.tokens.hasNext()) {
			if (this.next == null) {
				this.next = this.tokens.next();
				if (this.next == null) {
					continue;
				}
			}

			switch (this.next.getToken()) {
				case ATTR_NAME:
					attrName = this.next.getValue();
					// set just in case no value
					elem.setAttribute(attrName, null);

					// consume next
					this.next = null;
					break;

				case ATTR_BLOCK:
					if (attrName == null) {
						// TODO: syntax error
						continue;
					}

					// TODO: process blocks
					//parent.setAttribute(attrName, next.getBlock());
					attrName = null;

					// consume next
					this.next = null;
					break;

				case ATTR_LITERAL:
					if (attrName == null) {
						// TODO: syntax error
						continue;
					}

					elem.setAttribute(attrName, new LiteralNode(this.next.getValue()));
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
}
