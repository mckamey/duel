package org.duelengine.duel.ast;

public class PARTCommandNode extends CommandNode {

	public static final String EXT_NAME = "part";
	private static final String NAME = "$part";
	private static final CommandName CMD = CommandName.PART;
	private String name;

	public PARTCommandNode() {
		super(CMD, NAME, false);
	}

	public PARTCommandNode(AttributeNode[] attr, Node... children) {
		super(CMD, NAME, false, attr, children);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;

		if (name == null) {
			super.removeAttribute("name");
		} else {
			super.setAttribute("name", new LiteralNode(value));
		}
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
		if (!name.equalsIgnoreCase("name")) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on PART declaration: "+name);
		}
		if (value != null && !(value instanceof LiteralNode)) {
			// Syntax error
			throw new IllegalArgumentException("PART name must be a string literal: "+value.getClass());
		}

		this.setName(value == null ? null : ((LiteralNode)value).getValue());
	}
}
