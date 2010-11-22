package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

public class VIEWCommandNode extends CommandNode {

	public static final String EXT_NAME = "view";
	private static final String NAME = "$view";
	private static final CommandName CMD = CommandName.VIEW;
	private String name;

	public VIEWCommandNode(int index, int line, int column) {
		super(CMD, NAME, false, index, line, column);
	}

	public VIEWCommandNode(AttributePair[] attr, DuelNode... children) {
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
	public void addAttribute(AttributePair attr)
		throws NullPointerException {

		if (attr == null) {
			throw new NullPointerException("attr");
		}

		this.setAttribute(attr.getName(), attr.getValue());
	}

	@Override
	public void setAttribute(String name, DuelNode value) {
		if (name == null || name.length() == 0) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase("name")) {
			throw new InvalidNodeException("Attribute invalid on VIEW declaration: "+name, value);
		}
		if (value != null && !(value instanceof LiteralNode)) {
			// Syntax error
			throw new InvalidNodeException("VIEW name must be a string literal: "+value.getClass(), value);
		}

		this.name = (value == null ? null : ((LiteralNode)value).getValue());
	}
}
