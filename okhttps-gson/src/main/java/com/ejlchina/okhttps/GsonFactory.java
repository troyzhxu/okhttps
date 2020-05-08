package com.ejlchina.okhttps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class GsonFactory implements JsonFactory {

	private final Gson gson;
	
	public GsonFactory() {
		this(new Gson());
	}
	
	public GsonFactory(Gson gson) {
		this.gson = gson;
	}

	@Override
	public JsonObj newJsonObj(String json) {
		if (json != null) {
			return new GsonObj(gson.fromJson(json, JsonObject.class));
		}
		return null;
	}

	@Override
	public JsonArr newJsonArr(String json) {
		if (json != null) {
			return new GsonArr(gson.fromJson(json, JsonArray.class));
		}
		return null;
	}

	@Override
	public String toJsonStr(Object bean) {
		return gson.toJson(bean);
	}

	@Override
	public String toJsonStr(Object bean, String dateFormat) {
		return gson.newBuilder().setDateFormat(dateFormat).create().toJson(bean);
	}

	@Override
	public <T> T jsonToBean(Class<T> type, String json) {
		if (json != null) {
			return gson.fromJson(json, type);
		}
		return null;
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, String json) {
		if (json != null) {
			T[] beans = gson.fromJson(json, TypeToken.getArray(type).getType());
			List<T> list = new ArrayList<>();
			Collections.addAll(list, beans);
			return list;
		}
		return null;
	}
	
}














