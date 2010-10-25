package org.duelengine.duel.parsing;

import java.util.*;
import org.duelengine.duel.ast.*;

public class DuelParser {

	//private final Stack<String> elemStack = new Stack<String>();

	public List<Node> parse(Iterator<DuelToken> tokens) {
		if (tokens == null) {
			throw new NullPointerException("tokens");
		}

		List<Node> nodes = new ArrayList<Node>();
		while (tokens.hasNext()) {
			DuelToken root = tokens.next();
			if (root == null) {
				continue;
			}

			switch (root.getToken()) {
				case LITERAL:
					this.parseLiteral(nodes, root);
					break;
				case END:
					continue;
			}
		}
	
		return nodes;
	}

	private void parseLiteral(List<Node> nodes, DuelToken root) {
		Node last = nodes.isEmpty() ? null : nodes.get(nodes.size()-1);

		if (last instanceof LiteralNode) {
			LiteralNode lastLit = ((LiteralNode)last);

			// perform constant folding of literal strings
			lastLit.setValue(lastLit.getValue() + root.getValue());
		} else {
			// add directly to output
			nodes.add(new LiteralNode(root.getValue()));
		}
	}
}
