package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FastJsonArr implements JsonArr {

	private JSONArray json;
	
	public FastJsonArr(JSONArray json) {
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
		JSONObject subJson = json.getJSONObject(index);
		if (subJson != null) {
			return new FastJsonObj(subJson);
		}
		return null;
	}

	@Override
	public JsonArr getJsonArr(int index) {
		JSONArray subJson = json.getJSONArray(index);
		if (subJson != null) {
			return new FastJsonArr(subJson);
		}
		return null;
	}

	@Override
	public Boolean getBool(int index) {
		return json.getBoolean(index);
	}

	@Override
	public boolean getBoolVal(int index) {
		return json.getBooleanValue(index);
	}

	@Override
	public Integer getInt(int index) {
		return json.getInteger(index);
	}

	@Override
	public int getIntVal(int index) {
		return json.getIntValue(index);
	}

	@Override
	public Short getShort(int index) {
		return json.getShort(index);
	}

	@Override
	public short getShortVal(int index) {
		return json.getShortValue(index);
	}

	@Override
	public Float getFloat(int index) {
		return json.getFloat(index);
	}

	@Override
	public float getFloatVal(int index) {
		return json.getFloatValue(index);
	}

	@Override
	public Double getDouble(int index) {
		return json.getDouble(index);
	}

	@Override
	public double getDoubleVal(int index) {
		return json.getDoubleValue(index);
	}

	@Override
	public String getString(int index) {
		return json.getString(index);
	}

}
