package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

/**
 * Implements the looping command construct
 */
public class FORCommandNode extends CommandNode {

	public static final String EXT_NAME = "for";
	private static final String NAME = "$for";
	private static final CommandName CMD = CommandName.FOR;

	public static final String EACH = "each";
	public static final String IN = "in";
	public static final String COUNT = "count";
	public static final String DATA = "data";

	public FORCommandNode(int index, int line, int column) {
		super(CMD, NAME, true, index, line, column);
	}

	public FORCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, true, attr, children);
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
	public void setAttribute(String name, DuelNode value) {
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase(EACH) &&
			!name.equalsIgnoreCase(IN) &&
			!name.equalsIgnoreCase(COUNT) &&
			!name.equalsIgnoreCase(DATA)) {

			throw new InvalidNodeException("Invalid attribute on FOR command: "+name, value);
		}

		super.setAttribute(name, value);
	}
}
