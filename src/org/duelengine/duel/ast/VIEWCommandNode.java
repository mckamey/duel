package org.duelengine.duel.ast;

public class VIEWCommandNode extends CommandNode {

	public static final String EXT_NAME = "view";
	private static final String NAME = "$view";
	private static final CommandName CMD = CommandName.VIEW;
	private String name;

	public VIEWCommandNode() {
		super(CMD, NAME, false);
	}

	public VIEWCommandNode(AttributeNode[] attr, Node... children) {
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
		if (name == null || name.length() == 0) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase("name")) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on VIEW declaration: "+name);
		}
		if (value != null && !(value instanceof LiteralNode)) {
			// Syntax error
			throw new IllegalArgumentException("VIEW name must be a string literal: "+value.getClass());
		}

		this.name = (value == null ? null : ((LiteralNode)value).getValue());
	}
}
