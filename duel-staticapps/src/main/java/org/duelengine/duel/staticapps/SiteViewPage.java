package org.duelengine.duel.staticapps;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class SiteViewPage {

	private String view;
	private Object data;
	private Map<String, Object> extras;

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
}
