package org.duelengine.duel.ast;

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

	public FORCommandNode(AttributePair[] attr, Node... children) {
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

		this.setAttribute(attr.getName(), attr.getValue());
	}

	@Override
	public void setAttribute(String name, Node value) {
		if (name == null || name.length() == 0) {
			throw new NullPointerException("name");
		}
		if (!name.equalsIgnoreCase(EACH) &&
			!name.equalsIgnoreCase(IN) &&
			!name.equalsIgnoreCase(COUNT) &&
			!name.equalsIgnoreCase(DATA)) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on FOR command: "+name);
		}

		super.setAttribute(name, value);
	}
}
