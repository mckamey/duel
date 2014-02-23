package org.duelengine.duel.ast;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class ElementNode extends ContainerNode {

	private static final String EXT_INIT = "init";
	private static final String CMD_INIT = "$init";
	private static final String EXT_LOAD = "load";
	private static final String CMD_LOAD = "$load";

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.ast.HTMLTags";
	private static final Set<String> voidTags;
	private static final Set<String> linkTags;
	private static final Set<String> linkAttrs;
	private static final Set<String> boolAttrs;

	private final String tagName;
	private final boolean isVoid;
	private final boolean isLinkableTag;
	private final Map<String, DuelNode> attributes = new LinkedHashMap<String, DuelNode>();

	static {
		// definitions maintained in HTMLTags.properties
		ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE, Locale.ROOT);

		String[] items = (config != null) && config.containsKey("voidTags") ?
			config.getString("voidTags").split("\\s+") : new String[0];

		Set<String> set = new HashSet<String>(items.length);
		for (String value : items) {
			set.add(value);
		}

		voidTags = set;

		items = (config != null) && config.containsKey("linkTags") ?
			config.getString("linkTags").split("\\s+") : new String[0];

		set = new HashSet<String>(items.length);
		for (String value : items) {
			set.add(value);
		}

		linkTags = set;

		items = (config != null) && config.containsKey("linkAttrs") ?
			config.getString("linkAttrs").split("\\s+") : new String[0];

		set = new HashSet<String>(items.length);
		for (String value : items) {
			set.add(value);
		}

		linkAttrs = set;

		items = (config != null) && config.containsKey("boolAttrs") ?
			config.getString("boolAttrs").split("\\s+") : new String[0];

		set = new HashSet<String>(items.length);
		for (String value : items) {
			set.add(value);
		}

		boolAttrs = set;
	}

	public ElementNode(String name, int index, int line, int column) {
		super(index, line, column);

		tagName = name;
		isVoid = (name == null) || voidTags.contains(name);
		isLinkableTag = (name != null) && linkTags.contains(name);
	}

	public ElementNode(String name) {
		tagName = name;
		isVoid = (name == null) || voidTags.contains(name);
		isLinkableTag = (name != null) && linkTags.contains(name);
	}

	public ElementNode(String name, AttributePair[] attr, DuelNode... children) {
		super(children);

		tagName = name;
		isVoid = (name == null) || voidTags.contains(name);
		isLinkableTag = (name != null) && linkTags.contains(name);

		if (attr != null) {
			for (AttributePair a : attr) {
				attributes.put(mapAttrName(a.getName()), a.getValue());
			}
		}
	}

	public String getTagName() {
		return tagName;
	}

	public boolean canHaveChildren() {
		return !isVoid;
	}

	public boolean isLinkAttribute(String name) {
		return isLinkableTag && linkAttrs.contains(name);
	}

	public boolean isBoolAttribute(String name) {
		return boolAttrs.contains(name);
	}

	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}

	public void addAttribute(AttributePair attr)
		throws NullPointerException {
		if (attr == null) {
			throw new NullPointerException("attr");
		}
		attributes.put(mapAttrName(attr.getName()), attr.getValue());
	}

	public DuelNode getAttribute(String name) {
		if (!attributes.containsKey(name)) {
			return null;
		}
		return attributes.get(name);
	}

	public void setAttribute(String name, DuelNode value) {
		attributes.put(mapAttrName(name), value);
	}

	public DuelNode removeAttribute(String name) {
		if (!attributes.containsKey(name)) {
			return null;
		}
		return attributes.remove(name);
	}

	public void clearAttributes() {
		attributes.clear();
	}

	public boolean isSelf(String tag) {
		return (tagName == null) ? (tag == null) : tagName.equalsIgnoreCase(tag);
	}

	public boolean isAncestor(String tag) {
		ContainerNode parent = getParent();

		while (parent != null) {
			if (parent instanceof ElementNode && ((ElementNode)parent).isSelf(tag)) {
				return true;
			}
			parent = parent.getParent();
		}

		return false;
	}

	public boolean isAncestorOrSelf(String tag) {
		return isSelf(tag) || isAncestor(tag);
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
		buffer.append("<").append(tagName);

		for (String name : attributes.keySet()) {
			buffer
				.append(' ')
				.append(name)
				.append("=\"")
				.append(getAttribute(name))
				.append('"');
		}

		if (hasChildren()) {
			buffer.append('>');
			super.toString(buffer).append("</").append(tagName);
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
		if (tagName == null ? that.tagName != null : !tagName.equals(that.tagName)) {
			return false;
		}

		for (String name : attributes.keySet()) {
			if (!that.attributes.containsKey(name)) {
				return false;
			}

			DuelNode thisValue = getAttribute(name);
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

		int hash = (tagName == null) ? 0 : tagName.hashCode();
		if (attributes != null) {
			hash = hash * HASH_PRIME + attributes.hashCode();
		}
		return hash;
	}
}
