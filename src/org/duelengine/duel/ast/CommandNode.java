package org.duelengine.duel.ast;

import java.util.*;

public abstract class CommandNode extends ElementNode {

	private final boolean codeAttrs;
	private final CommandName command;

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs) {
		super(name);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, AttributeNode[] attr) {
		super(name, (attr != null) ? Arrays.asList(attr) : null, null);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, Collection<AttributeNode> attr) {
		super(name, attr, null);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, AttributeNode[] attr, Node[] children) {
		super(name, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, Collection<AttributeNode> attr, Collection<Node> children) {
		super(name, attr, children);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
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

		// ensure all command attributes are code blocks
		if (this.codeAttrs && value instanceof LiteralNode) {
			value = new ExpressionNode(((LiteralNode)value).getValue());
		}

		super.setAttribute(name, value);
	}
}
