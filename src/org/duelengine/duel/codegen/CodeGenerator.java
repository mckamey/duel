package org.duelengine.duel.codegen;

import java.io.*;
import org.duelengine.duel.ast.*;

public interface CodeGenerator {

	public CodeGenSettings getSettings();

	public String getFileExtension();

	public void write(Writer writer, ViewRootNode view);

	public void write(Writer writer, ViewRootNode[] views);

	public void write(Writer writer, Iterable<ViewRootNode> views);
}
