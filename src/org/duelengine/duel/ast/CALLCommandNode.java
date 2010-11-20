package org.duelengine.duel.ast;

public class CALLCommandNode extends CommandNode {

	public static final String EXT_NAME = "call";
	private static final String NAME = "$call";
	private static final CommandName CMD = CommandName.CALL;
	public static final String VIEW = "view";
	public static final String DATA = "data";
	public static final String INDEX = "index";
	public static final String COUNT = "count";
	public static final String KEY = "key";
	private PARTCommandNode defaultPart;

	public CALLCommandNode() {
		super(CMD, NAME, true);
	}

	public CALLCommandNode(AttributeNode[] attr, Node... children) {
		super(CMD, NAME, true, attr, children);
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
		if (!name.equalsIgnoreCase(VIEW) &&
			!name.equalsIgnoreCase(DATA) &&
			!name.equalsIgnoreCase(INDEX) &&
			!name.equalsIgnoreCase(COUNT) &&
			!name.equalsIgnoreCase(KEY)) {

			// Syntax error
			throw new IllegalArgumentException("Attribute invalid on CALL command: "+name);
		}

		super.setAttribute(name, value);
	}

	@Override
	public void appendChild(Node child) {
		if (child instanceof PARTCommandNode) {
			super.appendChild(child);
			return;
		}

		// add a default part which contains anything else
		if (this.defaultPart == null) {
			// ignore inter-element whitespace nodes if
			// no other inter-element content has been added
			if (child instanceof LiteralNode &&
				isNullOrWhiteSpace(((LiteralNode)child).getValue())) {
				return;
			}

			// unnamed part contains the extra nodes
			this.defaultPart = new PARTCommandNode();
			super.appendChild(this.defaultPart);
		}

		this.defaultPart.appendChild(child);
	}

	public static boolean isNullOrWhiteSpace(String value) {
		if (value == null) {
			return true;
		}

		for (int i=0, length=value.length(); i<length; i++) {
			switch (value.charAt(i)) {
				case ' ':		// Space
				case '\t':		// Tab
				case '\n':		// LF
				case '\r':		// CR
				case '\u000C':	// FF
					continue;
				default:
					return false;
			}
		}
		
		return true;
	}
}
