package com.ejlchina.okhttps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GsonArray implements Array {

	private final JsonArray json;
	
	public GsonArray(JsonArray json) {
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
	public Mapper getMapper(int index) {
		JsonElement subJson = json.get(index);
		if (subJson != null && subJson.isJsonObject()) {
			return new GsonMapper(subJson.getAsJsonObject());
		}
		return null;
	}

	@Override
	public Array getArray(int index) {
		JsonElement subJson = json.get(index);
		if (subJson != null && subJson.isJsonArray()) {
			return new GsonArray(subJson.getAsJsonArray());
		}
		return null;
	}

	@Override
	public boolean getBool(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsBoolean();
		}
		return false;
	}

	@Override
	public int getInt(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsInt();
		}
		return 0;
	}

	@Override
	public long getLong(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsLong();
		}
		return 0;
	}
	
	@Override
	public float getFloat(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsFloat();
		}
		return 0;
	}

	@Override
	public double getDouble(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsDouble();
		}
		return 0;
	}

	@Override
	public String getString(int index) {
		JsonElement val = json.get(index);
		if (val != null && val.isJsonPrimitive()) {
			return val.getAsString();
		}
		return null;
	}

	@Override
	public String toString() {
		return json.toString();
	}

}
