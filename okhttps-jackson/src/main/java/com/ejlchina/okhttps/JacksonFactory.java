package com.ejlchina.okhttps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ejlchina.okhttps.internal.HttpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

public class JacksonFactory implements JsonFactory {

	private ObjectMapper objectMapper;
	
	public JacksonFactory() {
		this(new ObjectMapper());
	}
	
	public JacksonFactory(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public JsonObj newJsonObj(String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonArr newJsonArr(String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toJsonStr(Object bean) {
		try {
			return objectMapper.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			throw new HttpException("Java Bean [" + bean + "] Jackson 序列化异常", e);
		}
	}

	@Override
	public String toJsonStr(Object bean, String dateFormat) {
		ObjectMapper mapper = objectMapper.copy();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setDateFormat(new SimpleDateFormat(dateFormat));
		try {
			return mapper.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			throw new HttpException("Java Bean [" + bean + "] Jackson 序列化异常", e);
		}
	}

	@Override
	public <T> T jsonToBean(Class<T> type, String json) {
		if (json != null) {
			try {
				return objectMapper.readValue(json, type);
			} catch (JsonProcessingException e) {
				throw new HttpException("Jackson 解析异常：" + json, e);
			}
		}
		return null;
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, String json) {
		if (json != null) {
			CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
			try {
				return objectMapper.readValue(json, javaType);
			} catch (JsonProcessingException e) {
				throw new HttpException("Jackson 解析异常：" + json, e);
			}
		}
		return null;
	}

}
