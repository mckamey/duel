package org.duelengine.duel.ast;

/**
 * Implements the mutually exclusive conditional command wrapper
 */
public class XORCommandNode extends CommandNode {

	public static final String EXT_NAME = "if";
	private static final String NAME = "$xor";
	private static final CommandName CMD = CommandName.XOR;

	private IFCommandNode lastCase;

	public XORCommandNode(int index, int line, int column) {
		super(CMD, NAME, true, index, line, column);
	}

	public XORCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, true, attr, children);
	}

	private IFCommandNode getLastCase() {
		if (lastCase == null) {
			lastCase = new IFCommandNode(getIndex(), getLine(), getColumn());
			super.appendChild(lastCase);
		}

		return lastCase;
	}

	@Override
	public boolean isSelf(String tag) {
		return EXT_NAME.equalsIgnoreCase(tag) || NAME.equalsIgnoreCase(tag);
	}

	@Override
	public void addAttribute(AttributePair attr) {
		// attributes all reside on IF commands
		getLastCase().addAttribute(attr);
	}

	@Override
	public void setAttribute(String name, DuelNode value) {
		// attributes all reside on IF commands
		getLastCase().setAttribute(name, value);
	}

	@Override
	public void appendChild(DuelNode child) {
		if (child instanceof IFCommandNode) {
			lastCase = (IFCommandNode)child;
			super.appendChild(child);

		} else {
			getLastCase().appendChild(child);
		}
	}
}
