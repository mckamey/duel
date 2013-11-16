package org.duelengine.duel.staticapps;

import java.util.Map;

import org.duelengine.duel.DuelView;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SiteViewPage {

	private String view;
	private Object data;
	private Map<String, Object> extras;
	private CacheManifest appCache;

	/**
	 * @return the view name
	 */
	@JsonProperty
	public String view() {
		return view;
	}

	/**
	 * @value the view name
	 */
	@JsonProperty
	public SiteViewPage view(String value) {
		view = value;
		return this;
	}

	/**
	 * Gets the view data
	 */
	@JsonProperty
	public Object data() {
		return data;
	}

	/**
	 * Sets the view data
	 */
	@JsonProperty
	public SiteViewPage data(Object value) {
		data = value;
		return this;
	}

	/**
	 * Gets the ambient data extras
	 */
	@JsonProperty
	public Map<String, Object> extras() {
		return extras;
	}

	/**
	 * Sets the ambient data extras
	 */
	@JsonProperty
	public SiteViewPage extras(Map<String, Object> value) {
		extras = value;
		return this;
	}

	@JsonProperty
	public CacheManifest appCache() {
		return appCache;
	}

	@JsonProperty
	public SiteViewPage appCache(CacheManifest value) {
		appCache = value;
		return this;
	}

	/**
	 * @return the view class
	 * @throws ClassNotFoundException 
	 */
	protected Class<? extends DuelView> viewClass(String serverPrefix, ClassLoader classLoader)
			throws ClassNotFoundException {

		if (view == null) {
			return null;
		}

		String type = view;
		if (serverPrefix != null && !serverPrefix.isEmpty()) {
			if (serverPrefix.endsWith(".")) {
				type = serverPrefix + type;
			} else {
				type = serverPrefix + '.' + type;
			}
		}

		return Class.forName(type, true, classLoader).asSubclass(DuelView.class);
	}

	/**
	 * @return the view instance
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	protected DuelView viewInstance(String serverPrefix, ClassLoader classLoader)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<? extends DuelView> viewClass = viewClass(serverPrefix, classLoader);

		return (viewClass != null) ? viewClass.newInstance() : null;
	}
}
