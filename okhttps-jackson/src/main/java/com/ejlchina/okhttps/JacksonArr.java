package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonArr implements JsonArr {

	private ArrayNode json;
	
	public JacksonArr(ArrayNode json) {
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
	public JsonObj getJsonOjb(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isObject()) {
			return new JacksonObj((ObjectNode) subJson);
		}
		return null;
	}

	@Override
	public JsonArr getJsonArr(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isArray()) {
			return new JacksonArr((ArrayNode) subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.asBoolean()) {
			return subJson.asBoolean();
		}
		return false;
	}

	@Override
	public int getInt(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.intValue();
		}
		return 0;
	}

	@Override
	public float getFloat(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.floatValue();
		}
		return 0;
	}

	@Override
	public double getDouble(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.doubleValue();
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
	public BigDecimal getBigDecimal(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.decimalValue();
		}
		return null;
	}

	@Override
	public BigInteger getBigInteger(int index) {
		JsonNode subJson = json.get(index);
		if (subJson != null && subJson.isNumber()) {
			return subJson.bigIntegerValue();
		}
		return null;
	}

}
