package org.duelengine.duel.maven;

import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.duelengine.duel.compiler.*;

/**
 * Generates client-side and server-side sources
 *
 * @goal generate
 * @phase generate-sources
 */
public class DuelMojo extends AbstractMojo {

	// http://maven.apache.org/ref/3.0.2/maven-model/maven.html#class_build

	/**
	 * The project currently being built.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Location of the template files.
	 * 
	 * @parameter default-value="${project.build.sourceDirectory}"
	 */
	private String inputFolder;

	/**
	 * Location of the generated client-side templates.
	 * 
	 * @parameter default-value="${project.build.directory}/generated-sources/duel/"
	 */
	private String outputClientFolder;

	/**
	 * Location of the generated server-side templates.
	 * 
	 * @parameter default-value="${project.build.directory}/generated-sources/duel/"
	 */
	private String outputServerFolder;

	/**
	 * Client-side template package prefix
	 * 
	 * @parameter
	 */
	private String clientPrefix;

	/**
	 * Server-side template class package prefix
	 * 
	 * @parameter
	 */
	private String serverPrefix;

    public void execute()
        throws MojoExecutionException {

	    Log log = this.getLog();
	    log.info("\tinputFolder="+this.inputFolder);
	    log.info("\toutputClientFolder="+this.outputClientFolder);
	    log.info("\toutputServerFolder="+this.outputServerFolder);
	    log.info("\tclientPrefix="+this.clientPrefix);
	    log.info("\tserverPrefix="+this.serverPrefix);

	    DuelCompiler compiler = new DuelCompiler();
	    compiler.setInputFolder(this.inputFolder);

	    if (this.outputClientFolder != null && !this.outputClientFolder.isEmpty()) {
		    compiler.setOutputClientFolder(this.outputClientFolder);
	    }

		if (this.outputServerFolder != null && !this.outputServerFolder.isEmpty()) {
			compiler.setOutputServerFolder(this.outputServerFolder);
		}

	    if (this.clientPrefix != null && !this.clientPrefix.isEmpty()) {
		    compiler.setClientPrefix(this.clientPrefix);
	    }

	    if (this.serverPrefix != null && !this.serverPrefix.isEmpty()) {
	        compiler.setServerPrefix(this.serverPrefix);
	    }

	    try {
		    compiler.execute();

		    this.project.addCompileSourceRoot(compiler.getOutputServerFolder()); 

	    } catch (IOException e) {
		    log.error(e);
	    }
    }
}
