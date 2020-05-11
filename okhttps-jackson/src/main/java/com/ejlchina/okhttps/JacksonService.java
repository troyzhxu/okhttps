package com.ejlchina.okhttps;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ejlchina.okhttps.internal.HttpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;

public class JacksonService implements JsonService {

	private ObjectMapper objectMapper;
	
	public JacksonService() {
		this(new ObjectMapper());
	}
	
	public JacksonService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Mapper toMapper(InputStream in) {
		try {
			JsonNode json = objectMapper.readTree(in);
			if (json.isObject()) {
				return new JacksonObj((ObjectNode) json);
			}
			if (json.isNull() || json.isMissingNode()) {
				return null;
			}
			throw new HttpException("不是 一个 json 对象：" + json);
		} catch (IOException e) {
			throw new HttpException("Jackson 解析异常", e);
		}
	}

	@Override
	public Array toArray(InputStream in) {
		try {
			JsonNode json = objectMapper.readTree(in);
			if (json.isArray()) {
				return new JacksonArr((ArrayNode) json);
			}
			if (json.isNull() || json.isMissingNode()) {
				return null;
			}
			throw new HttpException("不是 一个 json 数组：" + json);
		} catch (IOException e) {
			throw new HttpException("Jackson 解析异常", e);
		}
	}

	@Override
	public String serialize(Object bean) {
		try {
			return objectMapper.writeValueAsString(bean);
		} catch (JsonProcessingException e) {
			throw new HttpException("Java Bean [" + bean + "] Jackson 序列化异常", e);
		}
	}

	@Override
	public String serialize(Object bean, String dateFormat) {
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
	public <T> T toBean(Class<T> type, InputStream in) {
		try {
			return objectMapper.readValue(in, type);
		} catch (IOException e) {
			throw new HttpException("Jackson 解析异常", e);
		}
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in) {
		CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
		try {
			return objectMapper.readValue(in, javaType);
		} catch (IOException e) {
			throw new HttpException("Jackson 解析异常", e);
		}
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
