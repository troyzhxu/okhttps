package com.ejlchina.okhttps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonArray implements Array {

	private final ArrayNode json;
	
	public JacksonArray(ArrayNode json) {
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
	public Mapper getMapper(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isObject()) {
			return new JacksonMapper((ObjectNode) subJson);
		}
		return null;
	}

	@Override
	public Array getArray(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isArray()) {
			return new JacksonArray((ArrayNode) subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null) {
			return subJson.asBoolean(false);
		}
		return false;
	}

	@Override
	public int getInt(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null) {
			return subJson.asInt(0);
		}
		return 0;
	}

	@Override
	public long getLong(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null) {
			return subJson.asLong(0);
		}
		return 0;
	}
	
	@Override
	public float getFloat(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.floatValue();
		}
		if (subJson != null) {
			return (float) subJson.asDouble(0);
		}
		return 0;
	}

	@Override
	public double getDouble(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null) {
			return subJson.asDouble(0);
		}
		return 0;
	}

	@Override
	public String getString(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null) {
			return subJson.asText();
		}
		return null;
	}

	@Override
	public String toString() {
		return json.toString();
	}

}
