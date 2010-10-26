package org.duelengine.duel.ast;

import java.util.Collection;

public class DocumentNode extends ContainerNode {

	private DocTypeNode doctype;

	public DocumentNode() {
		super();
	}

	public DocumentNode(Node[] children) {
		super(children);
	}

	public DocumentNode(Collection<Node> children) {
		super(children);
	}

	public void setDocType(DocTypeNode doctype) {
		this.doctype = doctype;
	}

	public DocTypeNode getDocType() {
		return doctype;
	}

	@Override
	public void appendChild(Node child) {
		if (child instanceof DocTypeNode) {
			this.doctype = (DocTypeNode)child;
			child.setParent(this);
		} else {
			super.appendChild(child);
		}
	}

	@Override
	StringBuilder toString(StringBuilder buffer) {
		if (this.doctype != null) {
			this.doctype.toString(buffer);
		}
		return super.toString(buffer);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof DocumentNode)) {
			// includes null
			return false;
		}

		DocumentNode that = (DocumentNode)arg;
		if (this.doctype == null ? that.doctype != null : !this.doctype.equals(that.doctype)) {
			return false;
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode();
		if (this.doctype != null) {
			hash = hash * HASH_PRIME + this.doctype.hashCode();
		}
		return hash;
	}
}
