package org.duelengine.duel.ast;

import org.duelengine.duel.parsing.InvalidNodeException;

public class VIEWCommandNode extends CommandNode {

	public static final String EXT_NAME = "view";
	private static final String NAME = "$view";
	private static final CommandName CMD = CommandName.VIEW;
	private String name;
	private boolean clientOnly;
	private boolean serverOnly;

	public VIEWCommandNode(int index, int line, int column) {
		super(CMD, NAME, false, index, line, column);
	}

	public VIEWCommandNode(AttributePair[] attr, DuelNode... children) {
		super(CMD, NAME, false, attr, children);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public boolean isClientOnly() {
		return this.clientOnly;
	}

	public void setClientOnly(boolean value) {
		this.clientOnly = value;
	}

	public boolean isServerOnly() {
		return this.serverOnly;
	}

	public void setServerOnly(boolean value) {
		this.serverOnly = value;
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
		if ("name".equalsIgnoreCase(name)) {
			if (value != null && !(value instanceof LiteralNode)) {
				// Syntax error
				throw new InvalidNodeException("VIEW name must be a string literal: "+value.getClass(), value);
			}

			this.name = (value == null ? null : ((LiteralNode)value).getValue());

		} else if ("client-only".equalsIgnoreCase(name)) {
			this.clientOnly = true;

		} else if ("server-only".equalsIgnoreCase(name)) {
			this.serverOnly = true;

		} else {
			throw new InvalidNodeException("Attribute invalid on VIEW declaration: "+name, value);
		}
	}

	@Override
	StringBuilder toString(StringBuilder buffer) {
		buffer
			.append("<")
			.append(EXT_NAME);

		if (this.name != null && !this.name.isEmpty()) {
			buffer
				.append(" name=\"")
				.append(this.name)
				.append('"');
		}

		if (this.clientOnly) {
			buffer.append(" client-only");
		}

		if (this.serverOnly) {
			buffer.append(" server-only");
		}

		if (this.hasChildren()) {
			buffer.append('>');
			for (DuelNode child : this.getChildren()) {
				buffer.append(child);
			}
			buffer.append("</").append(EXT_NAME);
		} else {
			buffer.append(" /");
		}

		return buffer.append('>');
	}
}
