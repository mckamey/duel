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
					nodes.add(new LiteralNode(root.getValue()));
					break;
				case END:
					continue;
			}
		}
	
		return nodes;
	}
}
