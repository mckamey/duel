package org.duelengine.duel.ast;

public abstract class CommandNode extends ElementNode {

	private final boolean codeAttrs;
	private final CommandName command;

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs) {
		super(name);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, AttributeNode[] attr, Node... children) {
		super(name, null, children);

		this.command = cmd;
		this.codeAttrs = codeAttrs;

		if (attr != null) {
			for (AttributeNode a : attr) {
				this.setAttribute(a.getName(), a.getValue());
			}
		}
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

		super.setAttribute(name != null ? name.toLowerCase() : null, value);
	}
}
