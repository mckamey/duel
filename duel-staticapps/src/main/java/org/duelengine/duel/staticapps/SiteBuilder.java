package org.duelengine.duel.staticapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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
	private static final int BUFFER_SIZE = 64*1024;//64K
	private final ClassLoader classLoader;
	private final byte[] buffer = new byte[BUFFER_SIZE];

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

		// link transformer which caches list of URLs
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

		FormatPrefs formatPrefs = new FormatPrefs()
			.setEncoding(config.encoding())
			.setIndent(config.isDevMode() ? "\t" : "")
			.setNewline(config.isDevMode() ? "\n" : "");

		Map<String, SiteViewPage> views = config.views();
		if (views != null) {
			for (String targetPage : views.keySet()) {
				SiteViewPage sitePage = views.get(targetPage);
				log.info("Generating: "+sitePage.view()+" => "+targetPage);

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

					Map<String, Object> extras = sitePage.extras();
					if (extras != null) {
						// ambient client-side data
						context.putExtras(extras);
					}

					DuelView view = sitePage.viewInstance(config.serverPrefix(), classLoader);
					if (view != null) {
						view.render(context);
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

		Map<String, String> linkCache = linkInterceptor.getLinkCache();
		for (String key : linkCache.keySet()) {
			try {
				copyResource(sourceDir, targetDir, key, linkCache.get(key));

			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}
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
