package org.duelengine.duel.ast;

/**
 * Implements the single conditional command construct
 */
public class IFCommandNode extends CommandNode {

	public static final String EXT_NAME = "else";
	private static final String NAME = "$if";
	private static final CommandName CMD = CommandName.IF;
	private static final String TEST = "test";

	public IFCommandNode(int index, int line, int column) {
		super(CMD, NAME, true, index, line, column);
	}

	public IFCommandNode(AttributePair[] attr, Node... children) {
		super(CMD, NAME, true, attr, children);
	}

	public Node getTest() {
		return this.getAttribute(TEST);
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
	public void setAttribute(String name, Node value) {
		if (name == null || name.length() == 0) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase(TEST) &&
			!name.equalsIgnoreCase("if")) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on IF/ELSE command: "+name);
		}

		// normalize the attribute to test
		super.setAttribute(TEST, value);
	}
}
