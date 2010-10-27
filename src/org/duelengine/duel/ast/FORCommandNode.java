package org.duelengine.duel.ast;

import java.util.*;

/**
 * Implements the looping command construct
 */
public class FORCommandNode extends CommandNode {

	public static final String EXT_NAME = "for";
	private static final String NAME = "$for";
	private static final CommandName CMD = CommandName.FOR;

	public FORCommandNode() {
		super(CMD, NAME);
	}

	public FORCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public FORCommandNode(Collection<AttributeNode> attr) {
		super(CMD, NAME, attr, null);
	}

	public FORCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public FORCommandNode(Collection<AttributeNode> attr, Collection<Node> children) {
		super(CMD, NAME, attr, children);
	}

	@Override
	public boolean isSelf(String tag) {
		return EXT_NAME.equalsIgnoreCase(tag);
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
		if (name == null || !name.equalsIgnoreCase("each")) {
			// TODO: Syntax error
			return;
		}

		super.setAttribute(name, value);
	}
}
