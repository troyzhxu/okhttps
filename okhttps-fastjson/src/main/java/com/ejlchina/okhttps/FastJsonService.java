package com.ejlchina.okhttps;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.internal.HttpException;

import okio.Okio;

public class FastJsonService implements JsonService {

	private Charset charset;
	
	public FastJsonService() {
		this(StandardCharsets.UTF_8);
	}
	
	public FastJsonService(Charset charset) {
		this.charset = charset;
	}

	@Override
	public JsonObj newJsonObj(InputStream in) {
		return new FastJsonObj(JSON.parseObject(toString(in)));
	}

	@Override
	public JsonArr newJsonArr(InputStream in) {
		return new FastJsonArr(JSON.parseArray(toString(in)));
	}

	@Override
	public String toJsonStr(Object bean) {
		return JSON.toJSONString(bean);
	}

	@Override
	public String toJsonStr(Object bean, String dateFormat) {
		return JSON.toJSONStringWithDateFormat(bean, dateFormat);
	}

	@Override
	public <T> T jsonToBean(Class<T> type, InputStream in) {
		try {
			return JSON.parseObject(in, type);
		} catch (IOException e) {
			throw new HttpException("读取文本异常", e);
		}
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, InputStream in) {
		return JSON.parseArray(toString(in), type);
	}

	private String toString(InputStream in) {
		try {
			return Okio.buffer(Okio.source(in)).readString(charset);
		} catch (IOException e) {
			throw new HttpException("读取文本异常", e);
		}
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

}
