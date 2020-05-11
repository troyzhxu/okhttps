package com.ejlchina.okhttps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class GsonMsgConvertor implements MsgConvertor, ConvertProvider {

	private Gson gson;

	public GsonMsgConvertor() {
		this(new Gson());
	}
	
	public GsonMsgConvertor(Gson gson) {
		this.gson = gson;
	}

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public Mapper toMapper(InputStream in, Charset charset) {
		return new GsonMapper(gson.fromJson(new InputStreamReader(in), JsonObject.class));
	}

	@Override
	public Array toArray(InputStream in, Charset charset) {
		return new GsonArray(gson.fromJson(new InputStreamReader(in), JsonArray.class));
	}

	@Override
	public byte[] serialize(Object object, Charset charset) {
		return serialize(object, null, charset);
	}

	@Override
	public byte[] serialize(Object object, String dateFormat, Charset charset) {
		if (object instanceof byte[]) {
			return (byte[]) object;
		}
		if (object instanceof String) {
			return object.toString().getBytes(charset);
		}
		Gson gson = this.gson;
		if (dateFormat != null) {
			gson = gson.newBuilder().setDateFormat(dateFormat).create();
		}
		return gson.toJson(object).getBytes(charset);
	}

	@Override
	public <T> T toBean(Class<T> type, InputStream in, Charset charset) {
		return gson.fromJson(new InputStreamReader(in), type);
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
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














