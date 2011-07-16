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
	 * @parameter default-value="${project.basedir}/src/main/resources/views/"
	 */
	private String inputDir;

	/**
	 * Directory where webapp is output..
	 * 
	 * @parameter default-value="${project.build.directory}/${project.build.finalName}"
	 * @readonly
	 * @required
	 */
	private String outputDir;

	/**
	 * App-relative path of the generated client-side templates.
	 * 
	 * @parameter default-value="/js/views/"
	 */
	private String outputClientPath;

	/**
	 * Location of the generated server-side templates.
	 * 
	 * @parameter default-value="${project.build.directory}/generated-sources/duel/"
	 */
	private String outputServerDir;

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
	    log.info("\tinputDir="+this.inputDir);
	    log.info("\toutputServerDir="+this.outputServerDir);

	    if (this.outputClientPath == null || this.outputClientPath.isEmpty()) {
			this.outputClientPath = "/js/views/";
		} else {
			if (!this.outputClientPath.startsWith("/")) {
				this.outputClientPath = '/'+this.outputClientPath;
			}
			if (!this.outputClientPath.endsWith("/")) {
				this.outputClientPath += '/';
			}
		}
	    log.info("\toutputClientDir="+this.outputDir+this.outputClientPath);
	    log.info("\tclientPrefix="+this.clientPrefix);
	    log.info("\tserverPrefix="+this.serverPrefix);

	    DuelCompiler compiler = new DuelCompiler();
	    compiler.setInputDir(this.inputDir);

	    compiler.setOutputClientDir(this.outputDir+this.outputClientPath);

		if (this.outputServerDir != null && !this.outputServerDir.isEmpty()) {
			compiler.setOutputServerDir(this.outputServerDir);
		}

	    if (this.clientPrefix != null && !this.clientPrefix.isEmpty()) {
		    compiler.setClientPrefix(this.clientPrefix);
	    }

	    if (this.serverPrefix != null && !this.serverPrefix.isEmpty()) {
	        compiler.setServerPrefix(this.serverPrefix);
	    }

	    try {
		    compiler.execute();

		    this.project.addCompileSourceRoot(compiler.getOutputServerDir()); 

	    } catch (IOException e) {
		    log.error(e);
	    }
    }
}
