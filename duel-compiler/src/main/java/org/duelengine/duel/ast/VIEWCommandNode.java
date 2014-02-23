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
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public boolean isClientOnly() {
		return clientOnly;
	}

	public void setClientOnly(boolean value) {
		clientOnly = value;
	}

	public boolean isServerOnly() {
		return serverOnly;
	}

	public void setServerOnly(boolean value) {
		serverOnly = value;
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
	public void setAttribute(String attrName, DuelNode attrValue) {
		if (attrName == null || attrName.isEmpty()) {
			throw new NullPointerException("name");
		}
		if ("name".equalsIgnoreCase(attrName)) {
			if (attrValue != null && !(attrValue instanceof LiteralNode)) {
				// Syntax error
				throw new InvalidNodeException("VIEW name must be a string literal: "+attrValue.getClass(), attrValue);
			}

			name = (attrValue == null ? null : ((LiteralNode)attrValue).getValue());

		} else if ("client-only".equalsIgnoreCase(attrName)) {
			clientOnly = true;

		} else if ("server-only".equalsIgnoreCase(attrName)) {
			serverOnly = true;

		} else {
			throw new InvalidNodeException("Attribute invalid on VIEW declaration: "+attrName, attrValue);
		}
	}

	@Override
	StringBuilder toString(StringBuilder buffer) {
		buffer
			.append("<")
			.append(EXT_NAME);

		if (name != null && !name.isEmpty()) {
			buffer
				.append(" name=\"")
				.append(name)
				.append('"');
		}

		if (clientOnly) {
			buffer.append(" client-only");
		}

		if (serverOnly) {
			buffer.append(" server-only");
		}

		if (hasChildren()) {
			buffer.append('>');
			for (DuelNode child : getChildren()) {
				buffer.append(child);
			}
			buffer.append("</").append(EXT_NAME);
		} else {
			buffer.append(" /");
		}

		return buffer.append('>');
	}
}
