package org.duelengine.duel.codegen;

import org.duelengine.duel.codedom.CodeVariableReferenceExpression;

/**
 * Intermediate values needed to generate hybrid deferred execution attributes.
 */
class HybridDeferredAttribute {

	private CodeVariableReferenceExpression valRef;
	private String attrName;
	private String clientCode;
	private int argSize;

	public CodeVariableReferenceExpression getValueRef() {
		return valRef;
	}

	public HybridDeferredAttribute setValueRef(CodeVariableReferenceExpression value) {
		valRef = value;
		return this;
	}

	public String getAttrName() {
		return attrName;
	}

	public HybridDeferredAttribute setAttrName(String value) {
		attrName = value;
		return this;
	}

	public String getClientCode() {
		return clientCode;
	}

	public HybridDeferredAttribute setClientCode(String value) {
		clientCode = value;
		return this;
	}

	public int getArgSize() {
		return argSize;
	}

	public HybridDeferredAttribute setArgSize(int value) {
		argSize = value;
		return this;
	}
}
