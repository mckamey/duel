package org.duelengine.duel.ast;

import java.util.*;

public class ElementNode extends ContainerNode {

	private static final String EXT_INIT = "init";
	private static final String CMD_INIT = "$init";
	private static final String EXT_LOAD = "load";
	private static final String CMD_LOAD = "$load";

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.ast.HTMLTags";
	private static final Map<String, Boolean> voidTags;
	private static final Map<String, Boolean> linkTags;
	private static final Map<String, Boolean> linkAttrs;
	private static final Map<String, Boolean> boolAttrs;

	private final String tagName;
	private final boolean isVoid;
	private final boolean isLinkableTag;
	private final Map<String, DuelNode> attributes = new LinkedHashMap<String, DuelNode>();

	static {
		// definitions maintained in HTMLTags.properties
		ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE);

		String[] items = (config != null) && config.containsKey("voidTags") ?
			config.getString("voidTags").split("\\s+") : new String[0];

		Map<String, Boolean> map = new HashMap<String, Boolean>(items.length);
		for (String value : items) {
			map.put(value, true);
		}

		voidTags = map;

		items = (config != null) && config.containsKey("linkTags") ?
			config.getString("linkTags").split("\\s+") : new String[0];

		map = new HashMap<String, Boolean>(items.length);
		for (String value : items) {
			map.put(value, true);
		}

		linkTags = map;

		items = (config != null) && config.containsKey("linkAttrs") ?
			config.getString("linkAttrs").split("\\s+") : new String[0];

		map = new HashMap<String, Boolean>(items.length);
		for (String value : items) {
			map.put(value, true);
		}

		linkAttrs = map;

		items = (config != null) && config.containsKey("boolAttrs") ?
			config.getString("boolAttrs").split("\\s+") : new String[0];

		map = new HashMap<String, Boolean>(items.length);
		for (String value : items) {
			map.put(value, true);
		}

		boolAttrs = map;
	}

	public ElementNode(String name, int index, int line, int column) {
		super(index, line, column);

		this.tagName = name;
		this.isVoid = (name == null) || voidTags.containsKey(name);
		this.isLinkableTag = (name != null) && linkTags.containsKey(name);
	}

	public ElementNode(String name) {
		this.tagName = name;
		this.isVoid = (name == null) || voidTags.containsKey(name);
		this.isLinkableTag = (name != null) && linkTags.containsKey(name);
	}

	public ElementNode(String name, AttributePair[] attr, DuelNode... children) {
		super(children);

		this.tagName = name;
		this.isVoid = (name == null) || voidTags.containsKey(name);
		this.isLinkableTag = (name != null) && linkTags.containsKey(name);

		if (attr != null) {
			for (AttributePair a : attr) {
				this.attributes.put(mapAttrName(a.getName()), a.getValue());
			}
		}
	}

	public String getTagName() {
		return this.tagName;
	}

	public boolean canHaveChildren() {
		return !this.isVoid;
	}

	public boolean isLinkAttribute(String name) {
		return this.isLinkableTag && linkAttrs.containsKey(name);
	}

	public boolean isBoolAttribute(String name) {
		return boolAttrs.containsKey(name);
	}

	public boolean hasAttributes() {
		return !this.attributes.isEmpty();
	}

	public Set<String> getAttributeNames() {
		return this.attributes.keySet();
	}

	public void addAttribute(AttributePair attr)
		throws NullPointerException {
		if (attr == null) {
			throw new NullPointerException("attr");
		}
		this.attributes.put(mapAttrName(attr.getName()), attr.getValue());
	}

	public DuelNode getAttribute(String name) {
		if (!this.attributes.containsKey(name)) {
			return null;
		}
		return this.attributes.get(name);
	}

	public void setAttribute(String name, DuelNode value) {
		this.attributes.put(mapAttrName(name), value);
	}

	public DuelNode removeAttribute(String name) {
		if (!this.attributes.containsKey(name)) {
			return null;
		}
		return this.attributes.remove(name);
	}

	public void clearAttributes() {
		this.attributes.clear();
	}

	public boolean isSelf(String tag) {
		return (this.tagName == null) ? (tag == null) : this.tagName.equalsIgnoreCase(tag);
	}

	public boolean isAncestor(String tag) {
		ContainerNode parent = this.getParent();

		while (parent != null) {
			if (parent instanceof ElementNode && ((ElementNode)parent).isSelf(tag)) {
				return true;
			}
			parent = parent.getParent();
		}

		return false;
	}

	public boolean isAncestorOrSelf(String tag) {
		return this.isSelf(tag) || this.isAncestor(tag);
	}

	/**
	 * Maps the human-entered attribute to output command
	 * 
	 * @param name
	 * @return
	 */
	protected String mapAttrName(String name) {
		if (EXT_INIT.equalsIgnoreCase(name)) {
			return CMD_INIT;
		}
		if (EXT_LOAD.equalsIgnoreCase(name)) {
			return CMD_LOAD;
		}

		return name;
	}
	
	@Override
	StringBuilder toString(StringBuilder buffer) {
		buffer.append("<").append(this.tagName);

		for (String name : this.attributes.keySet()) {
			buffer
				.append(' ')
				.append(name)
				.append("=\"")
				.append(this.getAttribute(name))
				.append('"');
		}

		if (this.hasChildren()) {
			buffer.append('>');
			super.toString(buffer).append("</").append(this.tagName);
		} else {
			buffer.append(" /");
		}

		return buffer.append('>');
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof ElementNode)) {
			// includes null
			return false;
		}

		ElementNode that = (ElementNode)arg;
		if (this.tagName == null ? that.tagName != null : !this.tagName.equals(that.tagName)) {
			return false;
		}

		for (String name : this.attributes.keySet()) {
			if (!that.attributes.containsKey(name)) {
				return false;
			}

			DuelNode thisValue = this.getAttribute(name);
			DuelNode thatValue = that.getAttribute(name);

			if (thisValue == null ? thatValue != null : !thisValue.equals(thatValue)) {
				return false;
			}
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.tagName == null) ? 0 : this.tagName.hashCode();
		if (this.attributes != null) {
			hash = hash * HASH_PRIME + this.attributes.hashCode();
		}
		return hash;
	}
}
