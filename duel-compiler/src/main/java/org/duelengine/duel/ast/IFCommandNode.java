package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

/**
 * Implements the single conditional command construct
 */
public class IFCommandNode extends CommandNode {

	public static final String IF_ATTR = "if";
	public static final String EXT_NAME = "else";
	private static final String NAME = "$if";
	private static final CommandName CMD = CommandName.IF;
	public static final String TEST = "test";

	public IFCommandNode(int index, int line, int column) {
		super(CMD, NAME, true, index, line, column);
	}

	public IFCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, true, attr, children);
	}

	public CodeBlockNode getTest() {
		DuelNode node = this.getAttribute(TEST);
		if (node instanceof CodeBlockNode || node == null) {
			return (CodeBlockNode)node;
		}

		throw new InvalidNodeException("Unexpected conditional test attribute: "+node.getClass(), node);
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
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase(TEST) &&
			!name.equalsIgnoreCase("if")) {

			throw new InvalidNodeException("Attribute invalid on IF/ELSE command: "+name, value);
		}

		// normalize the attribute to test
		super.setAttribute(TEST, value);
	}
}
