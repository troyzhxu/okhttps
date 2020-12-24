package com.ejlchina.okhttps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JacksonMapper implements Mapper {

	private final ObjectNode json;
	
	public JacksonMapper(ObjectNode json) {
		this.json = json;
	}

	@Override
	public int size() {
		return json.size();
	}

	@Override
	public boolean isEmpty() {
		return json.isEmpty();
	}

	@Override
	public Mapper getMapper(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isObject()) {
			return new JacksonMapper((ObjectNode) subJson);
		}
		return null;
	}

	@Override
	public Array getArray(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isArray()) {
			return new JacksonArray((ArrayNode) subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null) {
			return subJson.asBoolean(false);
		}
		return false;
	}

	@Override
	public int getInt(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null) {
			return subJson.asInt(0);
		}
		return 0;
	}

	@Override
	public long getLong(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null) {
			return subJson.asLong(0);
		}
		return 0;
	}

	@Override
	public float getFloat(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.floatValue();
		}
		if (subJson != null) {
			return (float) subJson.asDouble(0);
		}
		return 0;
	}

	@Override
	public double getDouble(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null) {
			return subJson.asDouble(0);
		}
		return 0;
	}

	@Override
	public String getString(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null) {
			return subJson.asText();
		}
		return null;
	}

	@Override
	public boolean has(String key) {
		return json.has(key);
	}

	@Override
	public Set<String> keySet() {
		Iterator<String> it = json.fieldNames();
		Set<String> set = new HashSet<>();
		while (it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}

	@Override
	public String toString() {
		return json.toString();
	}

}
