package org.duelengine.duel.ast;

public class PARTCommandNode extends CommandNode {

	public static final String EXT_NAME = "part";
	private static final String NAME = "$part";
	private static final CommandName CMD = CommandName.PART;
	private static final String DEFAULT_NAME = "";
	private String name;

	public PARTCommandNode(int index, int line, int column) {
		super(CMD, NAME, false, index, line, column);

		this.setAttribute("name", null);
	}

	public PARTCommandNode(AttributePair[] attr, Node... children) {
		super(CMD, NAME, false, attr, children);

		if (this.name == null) {
			this.setAttribute("name", null);
		}
	}

	public String getName() {
		return this.name;
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
		if (!name.equalsIgnoreCase("name")) {
			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on PART declaration: "+name);
		}
		if (value != null && !(value instanceof LiteralNode)) {
			// Syntax error
			throw new IllegalArgumentException("PART name must be a string literal: "+value.getClass());
		}

		if (value == null) {
			value = new LiteralNode(DEFAULT_NAME, this.getIndex(), this.getLine(), this.getColumn());
		}

		String partName = ((LiteralNode)value).getValue();
		if (partName == null) {
			((LiteralNode)value).setValue(DEFAULT_NAME);
			partName = DEFAULT_NAME;
		}

		this.name = partName;
		super.setAttribute("name", value);
	}
}
