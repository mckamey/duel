package org.duelengine.duel.ast;

import java.util.*;

public class PARTCommandNode extends CommandNode {

	public static final String EXT_NAME = "part";
	private static final String NAME = "$part";
	private static final CommandName CMD = CommandName.PART;

	public PARTCommandNode() {
		super(CMD, NAME, false);
	}

	public PARTCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, false, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public PARTCommandNode(Iterable<AttributeNode> attr) {
		super(CMD, NAME, false, attr, null);
	}

	public PARTCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, false, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public PARTCommandNode(Iterable<AttributeNode> attr, Iterable<Node> children) {
		super(CMD, NAME, false, attr, children);
	}

	@Override
	public boolean isSelf(String tag) {
		return EXT_NAME.equalsIgnoreCase(tag) || NAME.equalsIgnoreCase(tag);
	}

	@Override
	public void addAttribute(AttributeNode attr)
		throws NullPointerException {

		if (attr == null) {
			throw new NullPointerException("attr");
		}

		this.setAttribute(attr.getName(), attr.getValue());
	}

	@Override
	public void setAttribute(String name, Node value) {
		if (name == null || name.length() == 0) {
			throw new NullPointerException("name");
		}

		if (!name.equalsIgnoreCase("name")) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on PART command: "+name);
		}

		super.setAttribute(name, value);
	}
}
