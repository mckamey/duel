package org.duelengine.duel.ast;

import java.util.*;

public class CALLCommandNode extends CommandNode {

	public static final String EXT_NAME = "call";
	private static final String NAME = "$call";
	private static final CommandName CMD = CommandName.CALL;

	public CALLCommandNode() {
		super(CMD, NAME);
	}

	public CALLCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public CALLCommandNode(Collection<AttributeNode> attr) {
		super(CMD, NAME, attr, null);
	}

	public CALLCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public CALLCommandNode(Collection<AttributeNode> attr, Collection<Node> children) {
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
			(!name.equalsIgnoreCase("view") &&
			!name.equalsIgnoreCase("model") &&
			!name.equalsIgnoreCase("index") &&
			!name.equalsIgnoreCase("count"))) {
			// TODO: Syntax error
			return;
		}

		super.setAttribute(name, value);
	}
}
