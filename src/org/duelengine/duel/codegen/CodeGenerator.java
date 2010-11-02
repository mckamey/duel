package org.duelengine.duel.codegen;

import java.io.*;

import org.duelengine.duel.ast.*;

public interface CodeGenerator {

	public String getFileExtension();

	public void write(Writer writer, ViewRootNode[] views) throws IOException;

	public void write(Writer writer, Iterable<ViewRootNode> views) throws IOException;
}
