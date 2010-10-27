package org.duelengine.duel.ast;

import java.util.*;

/**
 * Implements the single conditional command construct
 */
public class IFCommandNode extends CommandNode {

	public static final String EXT_NAME = "else";
	private static final String NAME = "$if";
	private static final CommandName CMD = CommandName.IF;

	public IFCommandNode() {
		super(CMD, NAME);
	}

	public IFCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public IFCommandNode(Collection<AttributeNode> attr) {
		super(CMD, NAME, attr, null);
	}

	public IFCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public IFCommandNode(Collection<AttributeNode> attr, Collection<Node> children) {
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
		if (name == null ||
			(!name.equalsIgnoreCase("test") &&
			!name.equalsIgnoreCase("if"))) {
			// TODO: Syntax error
			return;
		}

		// normalize the attribute to test
		super.setAttribute("test", value);
	}
}
