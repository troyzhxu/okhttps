package com.ejlchina.okhttps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class GsonService implements JsonService {

	private Gson gson;
	
	public GsonService() {
		this(new Gson());
	}
	
	public GsonService(Gson gson) {
		this.gson = gson;
	}

	@Override
	public JsonObj toJsonObj(InputStream in) {
		return new GsonObj(gson.fromJson(new InputStreamReader(in), JsonObject.class));
	}

	@Override
	public JsonArr toJsonArr(InputStream in) {
		return new GsonArr(gson.fromJson(new InputStreamReader(in), JsonArray.class));
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
	public <T> T jsonToBean(Class<T> type, InputStream in) {
		return gson.fromJson(new InputStreamReader(in), type);
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, InputStream in) {
		T[] beans = gson.fromJson(new InputStreamReader(in), TypeToken.getArray(type).getType());
		List<T> list = new ArrayList<>();
		Collections.addAll(list, beans);
		return list;
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}
	
}














