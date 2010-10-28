package org.duelengine.duel.ast;

import java.util.*;

/**
 * Implements the mutually exclusive conditional command wrapper
 */
public class XORCommandNode extends CommandNode {

	public static final String EXT_NAME = "if";
	private static final String NAME = "$xor";
	private static final CommandName CMD = CommandName.XOR;

	private IFCommandNode lastCase;

	public XORCommandNode() {
		super(CMD, NAME, true);
	}

	public XORCommandNode(AttributeNode[] attr) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public XORCommandNode(Collection<AttributeNode> attr) {
		super(CMD, NAME, true, attr, null);
	}

	public XORCommandNode(AttributeNode[] attr, Node[] children) {
		super(CMD, NAME, true, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public XORCommandNode(Collection<AttributeNode> attr, Collection<Node> children) {
		super(CMD, NAME, true, attr, children);
	}

	private IFCommandNode getLastCase() {
		if (this.lastCase == null) {
			this.lastCase = new IFCommandNode();
			super.appendChild(this.lastCase);
		}

		return this.lastCase;
	}

	@Override
	public boolean isSelf(String tag) {
		return EXT_NAME.equalsIgnoreCase(tag) || NAME.equalsIgnoreCase(tag);
	}

	@Override
	public void addAttribute(AttributeNode attr) {
		// attributes all reside on IF commands
		this.getLastCase().addAttribute(attr);
	}

	@Override
	public void setAttribute(String name, Node value) {
		// attributes all reside on IF commands
		this.getLastCase().setAttribute(name, value);
	}

	@Override
	public void appendChild(Node child) {
		if (child instanceof IFCommandNode) {
			this.lastCase = (IFCommandNode)child;
			super.appendChild(child);

		} else {
			this.getLastCase().appendChild(child);
		}
	}
}
