package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

public class CALLCommandNode extends CommandNode {

	public static final String EXT_NAME = "call";
	private static final String NAME = "$call";
	private static final CommandName CMD = CommandName.CALL;
	public static final String VIEW = "view";
	public static final String DATA = "data";
	public static final String INDEX = "index";
	public static final String COUNT = "count";
	public static final String KEY = "key";
	public static final String DEFER = "defer";

	private PARTCommandNode defaultPart;
	private boolean defer;

	public CALLCommandNode(int index, int line, int column) {
		super(CMD, NAME, true, index, line, column);
	}

	public CALLCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, true, attr, children);
	}
	
	public void setDefer(boolean value) {
		this.defer = value;
	}

	public boolean isDefer() {
		return this.defer;
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
		
		if (name.equalsIgnoreCase(DEFER)) {
			this.setDefer(true);
			return;
		}

		if (!name.equalsIgnoreCase(VIEW) &&
			!name.equalsIgnoreCase(DATA) &&
			!name.equalsIgnoreCase(INDEX) &&
			!name.equalsIgnoreCase(COUNT) &&
			!name.equalsIgnoreCase(KEY) &&
			!name.equalsIgnoreCase(IFCommandNode.IF_ATTR)) {

			throw new InvalidNodeException("Attribute invalid on CALL command: "+name, value);
		}

		super.setAttribute(name, value);
	}

	@Override
	public void appendChild(DuelNode child) {
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
			this.defaultPart = new PARTCommandNode(this.getIndex(), this.getLine(), this.getColumn());
			super.appendChild(this.defaultPart);
		}

		this.defaultPart.appendChild(child);
	}

	@Override
	public boolean replaceChild(DuelNode newChild, DuelNode oldChild) {
		if (oldChild == null || (oldChild instanceof PARTCommandNode && newChild instanceof PARTCommandNode)) {
			// allow direct swap of PARTCommandNodes
			return super.replaceChild(newChild, oldChild);
		}

		if (this.defaultPart == null) {
			// fail
			return false;
		}

		return this.defaultPart.replaceChild(newChild, oldChild);
	}
	
	private static boolean isNullOrWhiteSpace(String value) {
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
