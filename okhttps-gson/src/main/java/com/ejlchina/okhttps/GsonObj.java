package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonObj implements JsonObj {

	private JsonObject json;
	
	public GsonObj(JsonObject json) {
		this.json = json;
	}

	@Override
	public int size() {
		return json.size();
	}

	@Override
	public boolean isEmpty() {
		return json.size() == 0;
	}

	@Override
	public JsonObj getJsonOjb(String key) {
		JsonElement subJson = json.get(key);
		if (subJson != null) {
			return new GsonObj(subJson.getAsJsonObject());
		}
		return null;
	}

	@Override
	public JsonArr getJsonArr(String key) {
		JsonElement subJson = json.get(key);
		if (subJson != null) {
			return new GsonArr(subJson.getAsJsonArray());
		}
		return null;
	}

	@Override
	public boolean getBool(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsBoolean();
		}
		return false;
	}

	@Override
	public int getInt(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsInt();
		}
		return 0;
	}

	@Override
	public long getLong(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsLong();
		}
		return 0;
	}

	@Override
	public float getFloat(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsFloat();
		}
		return 0;
	}

	@Override
	public double getDouble(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsDouble();
		}
		return 0;
	}

	@Override
	public String getString(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsString();
		}
		return null;
	}

	@Override
	public BigDecimal getBigDecimal(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsBigDecimal();
		}
		return null;
	}

	@Override
	public BigInteger getBigInteger(String key) {
		JsonElement val = json.get(key);
		if (val != null) {
			return val.getAsBigInteger();
		}
		return null;
	}

	@Override
	public boolean has(String key) {
		return json.has(key);
	}

	@Override
	public Set<String> keySet() {
		return json.keySet();
	}

	@Override
	public String toString() {
		return json.toString();
	}

}
