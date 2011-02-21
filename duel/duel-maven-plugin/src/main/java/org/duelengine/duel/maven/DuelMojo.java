package org.duelengine.duel.maven;

import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.duelengine.duel.compiler.*;

/**
 * Goal which touches a timestamp file.
 *
 * @goal compile
 * @phase process-sources
 */
public class DuelMojo extends AbstractMojo {

	// http://maven.apache.org/ref/3.0.2/maven-model/maven.html#class_build

	/**
	 * Location of the file.
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private String inputRoot;

	/**
	 * Location of the file.
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private String outputClientFolder;

	/**
	 * Location of the file.
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private String outputServerFolder;

	/**
	 * Client-side package prefix
	 * @parameter
	 */
	private String clientPrefix;

	/**
	 * Server-side class package prefix
	 * @parameter
	 */
	private String serverPrefix;

    public void execute()
        throws MojoExecutionException {

	    Log log = this.getLog();
	    /*
	    log.info("\tinputRoot="+this.inputRoot);
	    log.info("\toutputClientFolder="+this.outputClientFolder);
	    log.info("\toutputServerFolder="+this.outputServerFolder);
	    log.info("\tclientPrefix="+this.clientPrefix);
	    log.info("\tserverPrefix="+this.serverPrefix);
	    */

	    DuelCompiler compiler = new DuelCompiler();
	    compiler.setInputRoot(this.inputRoot);

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

	    } catch (IOException e) {
		    log.error(e);
	    }
    }
}
