package org.duelengine.duel.ast;

import java.util.*;

public abstract class CommandNode extends ElementNode {

	private final CommandName command;

	protected CommandNode(CommandName cmd, String name) {
		super(name);

		this.command = cmd;
	}

	protected CommandNode(CommandName cmd, String name, AttributeNode[] attr) {
		super(name, (attr != null) ? Arrays.asList(attr) : null, null);

		this.command = cmd;
	}

	protected CommandNode(CommandName cmd, String name, Collection<AttributeNode> attr) {
		super(name, attr, null);

		this.command = cmd;
	}

	protected CommandNode(CommandName cmd, String name, AttributeNode[] attr, Node[] children) {
		super(name, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);

		this.command = cmd;
	}

	protected CommandNode(CommandName cmd, String name, Collection<AttributeNode> attr, Collection<Node> children) {
		super(name, attr, children);

		this.command = cmd;
	}
	
	public CommandName getCommand() {
		return this.command;
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

		// in commands all attributes are actually code blocks
		if (value instanceof LiteralNode) {
			value = new ExpressionNode(((LiteralNode)value).getValue());
		}

		super.setAttribute(name, value);
	}
}
