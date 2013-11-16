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
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.duelengine.duel.staticapps.SiteConfig;
import org.duelengine.duel.utils.FileUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base implementation of Mojo which loads a staticapps config file.
 */
public abstract class SiteMojo extends AbstractMojo {

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

		try {
			SiteConfig config = loadConfig();

			ClassLoader classLoader = getClassLoader();

			execute(config, classLoader);

		} catch (Exception ex) {
			this.getLog().error(ex);
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}

	protected abstract void execute(SiteConfig config, ClassLoader classLoader)
			throws Exception;

	protected SiteConfig loadConfig()
			throws IOException {

		File configFile = new File(configPath);
		if (!configFile.isFile()) {
			throw new FileNotFoundException(configFile.getPath());
		}

		// deserialize config
		SiteConfig config = new ObjectMapper().reader(SiteConfig.class).readValue(configFile);

		// ensure paths are relative from config
		if (config.sourceDir() != null) {
			config.sourceDir(new File(configFile.getParentFile(), config.sourceDir()).getPath());
		}
		if (config.targetDir() != null) {
			config.targetDir(new File(configFile.getParentFile(), config.targetDir()).getPath());
		}

		return config;
	}

	protected ClassLoader getClassLoader() {
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
		return classLoader;
	}
}
