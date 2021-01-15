package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FastjsonMapper implements Mapper, Map<String, Object> {

	private final JSONObject json;
	
	public FastjsonMapper(JSONObject json) {
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
		JSONObject subJson = json.getJSONObject(key);
		if (subJson != null) {
			return new FastjsonMapper(subJson);
		}
		return null;
	}

	@Override
	public Array getArray(String key) {
		JSONArray subJson = json.getJSONArray(key);
		if (subJson != null) {
			return new FastjsonArray(subJson);
		}
		return null;
	}

	@Override
	public boolean getBool(String key) {
		return json.getBooleanValue(key);
	}

	@Override
	public int getInt(String key) {
		return json.getIntValue(key);
	}

	@Override
	public long getLong(String key) {
		return json.getLongValue(key);
	}
	
	@Override
	public float getFloat(String key) {
		return json.getFloatValue(key);
	}

	@Override
	public double getDouble(String key) {
		return json.getDoubleValue(key);
	}

	@Override
	public String getString(String key) {
		return json.getString(key);
	}

	@Override
	public boolean has(String key) {
		return json.containsKey(key);
	}

	@Override
	public Set<String> keySet() {
		return json.keySet();
	}

	@Override
	public boolean containsKey(Object key) {
		return json.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return json.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return json.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ?> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		return json.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return json.entrySet();
	}

	@Override
	public String toString() {
		return json.toJSONString();
	}

}
