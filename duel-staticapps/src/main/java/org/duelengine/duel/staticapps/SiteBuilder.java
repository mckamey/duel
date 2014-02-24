package org.duelengine.duel.staticapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.FormatPrefs;
import org.duelengine.duel.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteBuilder {

	private static final Logger log = LoggerFactory.getLogger(SiteBuilder.class);
	private static final Appendable NOOP_OUTPUT = new Appendable() {
		@Override
		public Appendable append(CharSequence value, int start, int end) throws IOException { return this; }

		@Override
		public Appendable append(char value) throws IOException { return this; }

		@Override
		public Appendable append(CharSequence value) throws IOException { return this; }
	};

	private static final int BUFFER_SIZE = 1024*1024;//1MB
	private final byte[] buffer = new byte[BUFFER_SIZE];
	private final ClassLoader classLoader;

	public SiteBuilder() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SiteBuilder(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void build(SiteConfig config)
			throws FileNotFoundException {

		if (config == null) {
			throw new NullPointerException("config");
		}

		File sourceDir = config.sourceDirFile();
		File targetDir = config.targetDirFile();

		if (sourceDir == null) {
			throw new NullPointerException("sourceDir");
		}
		if (targetDir == null) {
			throw new NullPointerException("targetDir");
		}
		if (!sourceDir.exists()) {
			throw new FileNotFoundException(sourceDir.getPath());
		}

		log.info("webapp source: "+sourceDir);
		log.info("static target: "+targetDir);

		File cdnDir = new File(targetDir, "cdn");
		if (cdnDir.isDirectory() && cdnDir.exists()) {
			log.info("Emptying existing CDN dir: "+cdnDir.getAbsolutePath());
			for (File child : cdnDir.listFiles()) {
				try {
					if (child.delete()) {
						log.trace("Deleting existing: "+child.getAbsolutePath());
					}
				} catch (Exception ex) {
					log.warn(ex.getMessage(), ex);
				}
			}
		}

		// link transformer which also caches list of URLs
		StaticLinkInterceptor linkInterceptor = createInterceptor(config);

		FormatPrefs formatPrefs = new FormatPrefs()
			.setEncoding(config.encoding())
			.setIndent(config.isDevMode() ? "\t" : "")
			.setNewline(config.isDevMode() ? "\n" : "");

		Map<String, String> linkCache = null;

		Map<String, SiteViewPage> views = config.views();
		if (views != null) {
			for (String targetPage : views.keySet()) {
				SiteViewPage sitePage = views.get(targetPage);
				log.info("Generating: "+sitePage.view()+" => "+targetPage);

				if (sitePage.appCache() != null) {
					if (linkCache == null) {
						linkCache = new HashMap<String, String>();
					}

					// aggregate and reset so manifest only contains this page's resources
					linkCache.putAll(linkInterceptor.getLinkCache());
					linkInterceptor.getLinkCache().clear();
				}

				FileWriter writer = null;
				try {
					File targetFile = new File(targetDir, targetPage);
					FileUtil.prepSavePath(targetFile);

					writer = new FileWriter(targetFile);

					DuelContext context = new DuelContext()
						.setFormat(formatPrefs)
						.setLinkInterceptor(linkInterceptor)
						.setData(sitePage.data())
						.setOutput(writer);

					Map<String, Object> extras = config.extras();
					if (extras != null) {
						// global ambient client-side data
						context.putExtras(extras);
					}

					extras = sitePage.extras();
					if (extras != null) {
						// page-level ambient client-side data
						context.putExtras(extras);
					}

					DuelView view = sitePage.viewInstance(config.serverPrefix(), classLoader);
					if (view != null) {
						view.render(context);
					}

					CacheManifest cacheManifest = sitePage.appCache();
					if (cacheManifest != null) {
						cacheManifest.addCachePaths(linkInterceptor.getLinkCache().values());
						new CacheManifestWriter().write(targetDir, cacheManifest);
					}

				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);

				} finally {
					if (writer != null) {
						try {
							writer.flush();
							writer.close();
						} catch (IOException ex) {}
					}
				}
			}
		}

		if ((linkCache != null) && !linkCache.isEmpty()) {
			// restore any previously stored values
			linkInterceptor.getLinkCache().putAll(linkCache);
		}
		linkCache = linkInterceptor.getLinkCache();

		// copy static resources which are blindly requested by userAgents (e.g., "robots.txt", "favicon.ico")
		String[] staticFiles = config.files();
		if (staticFiles != null) {
			for (String staticFile : staticFiles) {
				try {
					copyResource(sourceDir, targetDir, staticFile, staticFile);

				} catch (IOException ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}

		// ensure that all referenced files are copied
		for (String key : linkCache.keySet()) {
			try {
				copyResource(sourceDir, targetDir, key, linkCache.get(key));

			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	public void generateManifests(SiteConfig config)
			throws FileNotFoundException  {

		if (config == null) {
			throw new NullPointerException("config");
		}

		StaticLinkInterceptor linkInterceptor = null;

		for (SiteViewPage sitePage : config.views().values()) {
			if (sitePage.appCache() == null) {
				// only find dependencies if manifest needs generating
				continue;
			}

			if (linkInterceptor == null) {
				linkInterceptor = createInterceptor(config);

			} else if (!linkInterceptor.getLinkCache().isEmpty()) {
				linkInterceptor.getLinkCache().clear();
			}

			try {
				DuelContext context = new DuelContext()
					.setLinkInterceptor(linkInterceptor)
					.setData(sitePage.data())
					.setOutput(NOOP_OUTPUT);

				Map<String, Object> extras = config.extras();
				if (extras != null) {
					// global ambient client-side data
					context.putExtras(extras);
				}

				extras = sitePage.extras();
				if (extras != null) {
					// page-level ambient client-side data
					context.putExtras(extras);
				}

				DuelView view = sitePage.viewInstance(config.serverPrefix(), classLoader);
				if (view != null) {
					view.render(context);
				}

				sitePage.appCache().addCachePaths(linkInterceptor.getLinkCache().values());
				new CacheManifestWriter().write(config.targetDirFile(), sitePage.appCache());
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	private StaticLinkInterceptor createInterceptor(SiteConfig config) {
		StaticLinkInterceptor linkInterceptor = null;
		try {
			String bundleName = config.cdnMap();
			ResourceBundle cdnBundle =
				(bundleName == null) || bundleName.isEmpty() ? null :
				ResourceBundle.getBundle(bundleName, Locale.ROOT, classLoader);

			bundleName = config.cdnLinksMap();
			ResourceBundle cdnLinkBundle =
				(bundleName == null) || bundleName.isEmpty() ? null :
				ResourceBundle.getBundle(bundleName, Locale.ROOT, classLoader);

			linkInterceptor = new StaticLinkInterceptor(config.cdnHost(), cdnBundle, cdnLinkBundle, config.isDevMode());

		} catch (URISyntaxException ex) {
			log.error(ex.getMessage(), ex);
		}
		return linkInterceptor;
	}

	private void copyResource(File sourceDir, File targetDir, String path, String cdnPath)
			throws IOException {

		if (cdnPath.indexOf('?') >= 0) {
			cdnPath = cdnPath.substring(0, cdnPath.indexOf('?'));
		}
		if (cdnPath.indexOf('#') >= 0) {
			cdnPath = cdnPath.substring(0, cdnPath.indexOf('#'));
		}

		File source = new File(sourceDir, cdnPath);
		File target = new File(targetDir, cdnPath);
		if (!source.exists()) {
			// report but still copy the rest
			log.warn("Resource not found: "+source.getAbsolutePath());
			try {
				if (target.isFile() && target.exists() && target.delete()) {
					log.info("Deleted existing: "+target.getAbsolutePath());
				}
			} catch (Exception ex) {
				log.warn(ex.getMessage(), ex);
			}
			return;
		}
		if (!source.isFile()) {
			// report but still copy the rest
			log.warn("Resource not a file: "+source.getPath());
			try {
				if (target.isFile() && target.exists() && target.delete()) {
					log.info("Deleted existing: "+target.getAbsolutePath());
				}
			} catch (Exception ex) {
				log.warn(ex.getMessage(), ex);
			}
			return;
		}

		log.info("Copying "+path+" as "+cdnPath);
		FileUtil.copy(source, target, true, buffer);
	}
}
