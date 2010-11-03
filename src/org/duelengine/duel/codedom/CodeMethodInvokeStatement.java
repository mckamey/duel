package org.duelengine.duel.codedom;

/**
 * Emits a string into the output
 */
public class CodeMethodInvokeStatement extends CodeStatement {

	private String methodName;

	public CodeMethodInvokeStatement() {
	}

	public CodeMethodInvokeStatement(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String value) {
		this.methodName = value;
	}
}
