package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonObj implements JsonObj {

	private ObjectNode json;
	
	public JacksonObj(ObjectNode json) {
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
	public JsonObj getJsonOjb(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isObject()) {
			return new JacksonObj((ObjectNode) subJson);
		}
		return null;
	}

	@Override
	public JsonArr getJsonArr(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isArray()) {
			return new JacksonArr((ArrayNode) subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.asBoolean()) {
			return subJson.asBoolean();
		}
		return false;
	}

	@Override
	public int getInt(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.intValue();
		}
		return 0;
	}

	@Override
	public float getFloat(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.floatValue();
		}
		return 0;
	}

	@Override
	public double getDouble(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.doubleValue();
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
	public BigDecimal getBigDecimal(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.decimalValue();
		}
		return null;
	}

	@Override
	public BigInteger getBigInteger(String key) {
		JsonNode subJson = json.get(key);
		if (subJson != null && subJson.isNumber()) {
			return subJson.bigIntegerValue();
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

}
