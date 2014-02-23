package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

public class PARTCommandNode extends CommandNode {

	public static final String EXT_NAME = "part";
	private static final String NAME = "$part";
	private static final CommandName CMD = CommandName.PART;
	private static final String DEFAULT_NAME = "";
	private String name;

	public PARTCommandNode(int index, int line, int column) {
		super(CMD, NAME, false, index, line, column);

		setAttribute("name", null);
	}

	public PARTCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, false, attr, children);

		if (name == null) {
			setAttribute("name", null);
		}
	}

	public String getName() {
		return name;
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

		setAttribute(attr.getName(), attr.getValue());
	}

	@Override
	public void setAttribute(String attrName, DuelNode attrValue) {
		if (attrName == null || attrName.isEmpty()) {
			throw new NullPointerException("name");
		}
		if (!attrName.equalsIgnoreCase("name")) {
			throw new InvalidNodeException("Attribute invalid on PART declaration: "+attrName, attrValue);
		}
		if (attrValue != null && !(attrValue instanceof LiteralNode)) {
			throw new InvalidNodeException("PART name must be a string literal: "+attrValue.getClass(), attrValue);
		}

		if (attrValue == null) {
			attrValue = new LiteralNode(DEFAULT_NAME, getIndex(), getLine(), getColumn());
		}

		String partName = ((LiteralNode)attrValue).getValue();
		if (partName == null) {
			((LiteralNode)attrValue).setValue(DEFAULT_NAME);
			partName = DEFAULT_NAME;
		}

		name = partName;
		super.setAttribute("name", attrValue);
	}
}
