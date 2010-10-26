package org.duelengine.duel.parsing;

import java.util.*;

import org.duelengine.duel.ast.*;

public class DuelParser {

	public List<Node> parse(Iterator<DuelToken> tokens) {
		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		// TODO: build real document class
		ElementNode docFrag = new ElementNode();
		while (tokens.hasNext()) {
			this.parseNext(docFrag, tokens.next(), tokens);
		}
		return docFrag.getChildren();
	}

	private void parseNext(ElementNode parent, DuelToken next, Iterator<DuelToken> tokens) {
		if (next == null) {
			return;
		}

		switch (next.getToken()) {
			case LITERAL:
				this.parseLiteral(parent, next);
				break;

			case ELEM_BEGIN:
				this.parseElem(parent, next, tokens);
				break;

			case BLOCK:
				// TODO: process blocks
				break;

			default:
				// TODO: syntax error
				break;
		}
	}

	private void parseLiteral(ElementNode parent, DuelToken next) {
		Node last = parent.getLastChild();

		if (last instanceof LiteralNode) {
			LiteralNode lastLit = ((LiteralNode)last);

			// perform constant folding of literal strings
			lastLit.setValue(lastLit.getValue() + next.getValue());
		} else {
			// add directly to output
			parent.appendChild(new LiteralNode(next.getValue()));
		}
	}

	private void parseElem(ElementNode parent, DuelToken next, Iterator<DuelToken> tokens) {
		ElementNode elem = new ElementNode(next.getValue());
		parent.appendChild(elem);

		String attrName = null;

		while (tokens.hasNext()) {
			next = tokens.next();
			if (next == null) {
				continue;
			}

			switch (next.getToken()) {
				case ATTR_NAME:
					attrName = next.getValue();
					// set just in case no value
					elem.setAttribute(attrName, null);
					break;

				case ATTR_BLOCK:
					if (attrName == null) {
						// TODO: syntax error
						continue;
					}

					// TODO: process blocks
					//parent.setAttribute(attrName, next.getBlock());
					attrName = null;
					break;

				case ATTR_LITERAL:
					if (attrName == null) {
						// TODO: syntax error
						continue;
					}

					elem.setAttribute(attrName, new LiteralNode(next.getValue()));
					attrName = null;
					break;

				case ELEM_END:
					String tag = next.getValue();
					if (tag != null && tag.equals(elem.getTagName())) {
						return;
					}

					// TODO: auto-balance tags
					break;

				default:
					this.parseNext(elem, next, tokens);
					break;
			}
		}
	}
}
