package org.duelengine.duel.staticapps;

import java.io.File;
import java.util.Map;

import org.duelengine.duel.utils.FileUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class SiteConfig {

	private final static String TEXT_HTML = "text/html";
	private final static String UTF8 = "UTF-8";

	private String contentType;
	private String encoding;
	private String targetDir;
	private String sourceDir;
	private String serverPrefix;
	private String cdnHost;
	private String cdnMap;
	private String cdnLinksMap;
	private boolean isDevMode;
	private Map<String, Object> extras;
	private Map<String, SiteViewPage> views;
	private String[] files;

	// derivative values

	private File configFile;
	private File sourceDirFile;
	private File targetDirFile;

	public String contentType() {
		if (contentType == null || contentType.isEmpty()) {
			return TEXT_HTML;
		}
		return contentType;
	}

	public SiteConfig contentType(String value) {
		contentType = value;
		return this;
	}

	public String encoding() {
		if (encoding == null || encoding.isEmpty()) {
			return UTF8;
		}
		return encoding;
	}

	public SiteConfig encoding(String value) {
		encoding = value;
		return this;
	}

	/**
	 * Gets the target directory
	 */
	@JsonProperty
	public String targetDir() {
		return targetDir;
	}

	/**
	 * Sets the target directory
	 */
	@JsonProperty
	public SiteConfig targetDir(String value) {
		targetDir = value;

		if (value == null || value.isEmpty()) {
			targetDirFile = null;

		} else {
			targetDirFile = FileUtil.getCanonicalFile(targetDir);
		}
		return this;
	}

	/**
	 * Gets the web app directory
	 */
	@JsonProperty
	public String sourceDir() {
		return sourceDir;
	}

	/**
	 * Sets the web app directory
	 */
	@JsonProperty
	public SiteConfig sourceDir(String value) {
		sourceDir = value;

		if (value == null || value.isEmpty()) {
			sourceDirFile = null;

		} else {
			sourceDirFile = FileUtil.getCanonicalFile(sourceDir);
		}
		return this;
	}

	/**
	 * @return the server-side package name
	 */
	@JsonProperty
	public String serverPrefix() {
		return serverPrefix;
	}

	/**
	 * @value the server-side package name
	 */
	@JsonProperty
	public SiteConfig serverPrefix(String value) {
		serverPrefix = value;
		return this;
	}

	/**
	 * @return the CDN host name
	 */
	@JsonProperty
	public String cdnHost() {
		return cdnHost;
	}

	/**
	 * @value the CDN host name
	 */
	@JsonProperty
	public SiteConfig cdnHost(String value) {
		cdnHost = value;
		return this;
	}

	/**
	 * @return the name of the CDN map
	 */
	@JsonProperty
	public String cdnMap() {
		return cdnMap;
	}

	/**
	 * @value the name of the CDN map
	 */
	@JsonProperty
	public SiteConfig cdnMap(String value) {
		cdnMap = value;
		return this;
	}

	/**
	 * @return the name of the CDN links map
	 */
	@JsonProperty
	public String cdnLinksMap() {
		return cdnLinksMap;
	}

	/**
	 * @value the name of the CDN links map
	 */
	@JsonProperty
	public SiteConfig cdnLinksMap(String value) {
		cdnLinksMap = value;
		return this;
	}

	/**
	 * @return if should operate in dev mode
	 */
	@JsonProperty
	public boolean isDevMode() {
		return isDevMode;
	}

	/**
	 * @value if should operate in dev mode
	 */
	@JsonProperty
	public SiteConfig isDevMode(boolean value) {
		isDevMode = value;
		return this;
	}

	/**
	 * Gets the global ambient data extras
	 */
	@JsonProperty
	public Map<String, Object> extras() {
		return extras;
	}

	/**
	 * Sets the global ambient data extras
	 */
	@JsonProperty
	public SiteConfig extras(Map<String, Object> value) {
		extras = value;
		return this;
	}

	/**
	 * Gets the views to generate
	 */
	@JsonProperty
	public Map<String, SiteViewPage> views() {
		return views;
	}

	/**
	 * Sets the views to generate
	 */
	@JsonProperty
	public SiteConfig views(Map<String, SiteViewPage> value) {
		views = value;
		return this;
	}

	/**
	 * Gets the static files to copy
	 */
	@JsonProperty
	public String[] files() {
		return files;
	}

	/**
	 * Sets the static files to copy
	 */
	@JsonProperty
	public SiteConfig files(String[] value) {
		files = value;
		return this;
	}

	/* derived helpers -------------------------------------------*/

	/**
	 * @return the web app directory
	 */
	public File sourceDirFile() {
		return sourceDirFile;
	}

	/**
	 * @return the target output directory
	 */
	public File targetDirFile() {
		return targetDirFile;
	}

	/**
	 * Gets the config location
	 */
	public File configFile() {
		return configFile;
	}

	/**
	 * Sets the config location
	 */
	public SiteConfig configFile(File value) {
		configFile = (value != null) ? FileUtil.getCanonicalFile(value) : null;
		return this;
	}
}
