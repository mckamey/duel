package org.duelengine.duel.ast;

import java.util.*;

/**
 * Implements the looping command construct
 */
public class FORCommandNode extends CommandNode {

	public static final String EXT_NAME = "for";
	private static final String NAME = "$for";
	private static final CommandName CMD = CommandName.FOR;

	public static final String EACH = "each";
	public static final String IN = "in";
	public static final String COUNT = "count";
	public static final String MODEL = "model";

	public FORCommandNode() {
		super(CMD, NAME, true);
	}

	public FORCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public FORCommandNode(Iterable<AttributeNode> attr) {
		super(CMD, NAME, true, attr, null);
	}

	public FORCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public FORCommandNode(Iterable<AttributeNode> attr, Iterable<Node> children) {
		super(CMD, NAME, true, attr, children);
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
		if (!name.equalsIgnoreCase(EACH) &&
			!name.equalsIgnoreCase(IN) &&
			!name.equalsIgnoreCase(COUNT) &&
			!name.equalsIgnoreCase(MODEL)) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on FOR command: "+name);
		}

		super.setAttribute(name, value);
	}
}
