package org.duelengine.duel.staticapps.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.duelengine.duel.staticapps.SiteBuilder;
import org.duelengine.duel.staticapps.SiteConfig;
import org.duelengine.duel.utils.FileUtil;

/**
 * Generates static app from DUEL-based WAR
 *
 * @goal generate
 * @phase package
 */
public class SiteGeneratorMojo extends AbstractMojo {

	// http://maven.apache.org/ref/3.0.4/maven-model/maven.html#class_build

	/**
	 * The project currently being built.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The plugin descriptor
	 * 
	 * @parameter default-value="${descriptor}"
	 */
	private PluginDescriptor descriptor;

	/**
	 * Location of the configuration settings
	 * 
	 * @parameter default-value="${project.basedir}/staticapp.json"
	 */
	private String configPath;

	@Override
	public void setLog(Log log) {
		super.setLog(log);

		MavenLoggerAdapterFactory.setMavenLogger(log);
	};

	public void execute()
		throws MojoExecutionException {

		Log log = this.getLog();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			// http://stackoverflow.com/q/871708/43217
			log.info("adding build dependencies and target to classPath");
			ClassRealm realm = descriptor.getClassRealm();
			List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
			List<URL> runtimeUrls = new ArrayList<URL>(runtimeClasspathElements.size());
			for (String element : runtimeClasspathElements) {
				try {
					URL elementURL = FileUtil.getCanonicalFile(element).toURI().toURL();
					runtimeUrls.add(elementURL);
					if (realm != null) {
						realm.addURL(elementURL);
					}

				} catch (MalformedURLException ex) {
					log.error(ex);
				}
			}
			classLoader = new URLClassLoader(runtimeUrls.toArray(new URL[runtimeUrls.size()]), classLoader);
			Thread.currentThread().setContextClassLoader(classLoader);

		} catch (DependencyResolutionRequiredException ex) {
			log.error(ex);
		}

		try {
			File configFile = new File(configPath);

			if (!configFile.isFile()) {
				throw new FileNotFoundException(configFile.getPath());
			}

			// read config
			SiteConfig config = new ObjectMapper().reader(SiteConfig.class).readValue(configFile);

			// ensure paths are relative from config
			config
				.sourceDir(new File(configFile.getParentFile(), config.sourceDir()).getPath())
				.targetDir(new File(configFile.getParentFile(), config.targetDir()).getPath());

			// build site defined by config
			new SiteBuilder(classLoader).build(config);

		} catch (IOException e) {
			log.error(e);
		}
	}
}
