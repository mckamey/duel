package org.duelengine.duel.staticapps;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CacheManifest {

	private String manifest;
	private String version;
	private Set<String> cache;
	private Map<String, String> fallback;
	private Set<String> network;

	@JsonProperty
	public String manifest() {
		return manifest;
	}

	@JsonProperty
	public CacheManifest manifest(String value) {
		manifest = (value != null) ? value.trim() : null;
		return this;
	}

	@JsonProperty
	public String version() {
		return version;
	}

	@JsonProperty
	public CacheManifest version(String value) {
		version = (value != null) ? value.trim() : null;
		return this;
	}

	@JsonProperty
	public Set<String> cache() {
		return cache;
	}

	@JsonProperty
	public CacheManifest cache(Set<String> value) {
		cache = value;
		return this;
	}

	@JsonProperty
	public Map<String, String> fallback() {
		return fallback;
	}

	@JsonProperty
	public CacheManifest fallbacks(Map<String, String> value) {
		fallback = value;
		return this;
	}

	@JsonProperty
	public Set<String> network() {
		return network;
	}

	@JsonProperty
	public CacheManifest network(Set<String> value) {
		network = value;
		return this;
	}

	@JsonIgnore
	public CacheManifest addCachePaths(Collection<String> value) {
		if (cache == null) {
			cache = new HashSet<String>();
		}
		cache.addAll(value);
		return this;
	}
}
