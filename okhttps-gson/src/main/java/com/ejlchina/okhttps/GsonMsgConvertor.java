package com.ejlchina.okhttps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class GsonMsgConvertor implements MsgConvertor, ConvertProvider {

	private Gson gson;
	private Charset charset;

	public GsonMsgConvertor() {
		this(new Gson(), StandardCharsets.UTF_8);
	}
	
	public GsonMsgConvertor(Gson gson, Charset charset) {
		this.gson = gson;
		this.charset = charset;
	}

	@Override
	public String mediaType() {
		return "application/json; charset=" + charset.displayName();
	}

	@Override
	public Mapper toMapper(InputStream in) {
		return new GsonMapper(gson.fromJson(new InputStreamReader(in), JsonObject.class));
	}

	@Override
	public Array toArray(InputStream in) {
		return new GsonArray(gson.fromJson(new InputStreamReader(in), JsonArray.class));
	}

	@Override
	public byte[] serialize(Object bean) {
		return gson.toJson(bean).getBytes(charset);
	}

	@Override
	public byte[] serialize(Object bean, String dateFormat) {
		return gson.newBuilder().setDateFormat(dateFormat).create().toJson(bean).getBytes(charset);
	}

	@Override
	public <T> T toBean(Class<T> type, InputStream in) {
		return gson.fromJson(new InputStreamReader(in), type);
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in) {
		T[] beans = gson.fromJson(new InputStreamReader(in), TypeToken.getArray(type).getType());
		List<T> list = new ArrayList<>();
		Collections.addAll(list, beans);
		return list;
	}

	@Override
	public MsgConvertor getConvertor() {
		return new GsonMsgConvertor();
	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

}














