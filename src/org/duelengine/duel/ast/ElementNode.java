package org.duelengine.duel.ast;

import java.util.*;

public class ElementNode extends ContainerNode {

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.ast.HTMLTags";
	private static Map<String, Boolean> voidTags;

	private final String tagName;
	private final boolean isVoid;
	private final Map<String, Node> attributes = new LinkedHashMap<String, Node>();

	public ElementNode(String name) {
		this.tagName = name;
		this.isVoid = (name == null) || getVoidTags().containsKey(name);
	}

	public ElementNode(String name, AttributeNode[] attr) {
		this(name, (attr != null) ? Arrays.asList(attr) : null, null);
	}

	public ElementNode(String name, Iterable<AttributeNode> attr) {
		this(name, attr, null);
	}

	public ElementNode(String name, AttributeNode[] attr, Node[] children) {
		this(name, (attr != null) ? Arrays.asList(attr) : null, (children != null) ? Arrays.asList(children) : null);
	}

	public ElementNode(String name, Iterable<AttributeNode> attr, Iterable<Node> children) {
		super(children);

		this.tagName = name;
		this.isVoid = (name == null) ? true : getVoidTags().containsKey(name);

		if (attr != null) {
			for (AttributeNode a : attr) {
				this.attributes.put(a.getName(), a.getValue());
			}
		}
	}

	public String getTagName() {
		return this.tagName;
	}

	public boolean canHaveChildren() {
		return !this.isVoid;
	}
	
	public void addAttribute(AttributeNode attr)
		throws NullPointerException {
		if (attr == null) {
			throw new NullPointerException("attr");
		}
		this.attributes.put(attr.getName(), attr.getValue());
	}

	public Node getAttribute(String name) {
		if (!this.attributes.containsKey(name)) {
			return null;
		}
		return this.attributes.get(name);
	}

	public void setAttribute(String name, Node value) {
		this.attributes.put(name, value);
	}

	public Node removeAttribute(String name) {
		if (!this.attributes.containsKey(name)) {
			return null;
		}

		Node attr = this.attributes.get(name);;
		this.attributes.remove(name);
		return attr;
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

	private static Map<String, Boolean> getVoidTags() {

		if (voidTags != null) {
			return voidTags;
		}

		// definitions maintained in HTMLTags.properties
		ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE);

		String[] tags = (config != null) && config.containsKey("voidTags") ?
			config.getString("voidTags").split(",") : new String[0];

		Map<String, Boolean> map = new HashMap<String, Boolean>(tags.length);
		for (String value : tags) {
			map.put(value, true);
		}

		return (voidTags = map);
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

			Node thisValue = this.getAttribute(name);
			Node thatValue = that.getAttribute(name);

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
