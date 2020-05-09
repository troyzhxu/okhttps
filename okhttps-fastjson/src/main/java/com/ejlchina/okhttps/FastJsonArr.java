package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;

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
	public boolean getBool(int index) {
		return json.getBooleanValue(index);
	}

	@Override
	public int getInt(int index) {
		return json.getIntValue(index);
	}

	@Override
	public float getFloat(int index) {
		return json.getFloatValue(index);
	}

	@Override
	public double getDouble(int index) {
		return json.getDoubleValue(index);
	}

	@Override
	public String getString(int index) {
		return json.getString(index);
	}

	@Override
	public BigDecimal getBigDecimal(int index) {
		return json.getBigDecimal(index);
	}

	@Override
	public BigInteger getBigInteger(int index) {
		return json.getBigInteger(index);
	}

}
