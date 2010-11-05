package org.duelengine.duel.ast;

import java.util.*;

/**
 * Implements the single conditional command construct
 */
public class IFCommandNode extends CommandNode {

	public static final String EXT_NAME = "else";
	private static final String NAME = "$if";
	private static final CommandName CMD = CommandName.IF;
	private static final String TEST = "test";

	public IFCommandNode() {
		super(CMD, NAME, true);
	}

	public IFCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public IFCommandNode(Iterable<AttributeNode> attr) {
		super(CMD, NAME, true, attr, null);
	}

	public IFCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public IFCommandNode(Iterable<AttributeNode> attr, Iterable<Node> children) {
		super(CMD, NAME, true, attr, children);
	}

	public Node getTest() {
		return this.getAttribute(TEST);
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
		if (name == null ||
			(!name.equalsIgnoreCase(TEST) &&
			!name.equalsIgnoreCase("if"))) {
			// TODO: Syntax error
			return;
		}

		// normalize the attribute to test
		super.setAttribute(TEST, value);
	}
}
