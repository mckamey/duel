package org.duelengine.duel.ast;

import java.util.*;

public class ViewRootNode extends CommandNode {

	public static final String EXT_NAME = "view";
	private static final String NAME = "$view";
	private static final CommandName CMD = CommandName.VIEW;
	private String name;

	public ViewRootNode() {
		super(CMD, NAME, false);
	}

	public ViewRootNode(AttributeNode[] attr) {
		super(CMD, NAME, false, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public ViewRootNode(Iterable<AttributeNode> attr) {
		super(CMD, NAME, false, attr, null);
	}

	public ViewRootNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, false, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public ViewRootNode(Iterable<AttributeNode> attr, Iterable<Node> children) {
		super(CMD, NAME, false, attr, children);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
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
		if (name == null || !name.equalsIgnoreCase("name") || !(value instanceof LiteralNode)) {
			// TODO: Syntax error
			return;
		}

		this.name = ((LiteralNode)value).getValue();
	}
}
