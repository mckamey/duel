package org.duelengine.duel.ast;

public abstract class CommandNode extends ElementNode {

	private final boolean codeAttrs;
	private final CommandName command;

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, int index, int line, int column) {
		super(name, index, line, column);

		this.command = cmd;
		this.codeAttrs = codeAttrs;
	}

	protected CommandNode(CommandName cmd, String name, boolean codeAttrs, AttributePair[] attr, DuelNode... children) {
		super(name, null, children);

		this.command = cmd;
		this.codeAttrs = codeAttrs;

		if (attr != null) {
			for (AttributePair a : attr) {
				setAttribute(a.getName(), a.getValue());
			}
		}
	}

	public CommandName getCommand() {
		return command;
	}

	@Override
	public void addAttribute(AttributePair attr)
		throws NullPointerException {

		if (attr == null) {
			throw new NullPointerException("attr");
		}

		setAttribute(attr.getName(), attr.getValue());
	}

	@Override
	public void setAttribute(String name, DuelNode value) {

		// ensure all command attributes are code blocks
		if (codeAttrs && value instanceof LiteralNode) {
			value = new ExpressionNode(((LiteralNode)value).getValue(), value.getIndex(), value.getLine(), value.getColumn());
		}

		super.setAttribute(name != null ? name.toLowerCase() : null, value);
	}
}
