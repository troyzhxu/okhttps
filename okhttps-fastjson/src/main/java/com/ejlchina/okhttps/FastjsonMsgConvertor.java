package com.ejlchina.okhttps;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.internal.HttpException;

import okio.Okio;

public class FastjsonMsgConvertor implements MsgConvertor, ConvertProvider {

	private Charset charset;
	
	public FastjsonMsgConvertor() {
		this(StandardCharsets.UTF_8);
	}
	
	public FastjsonMsgConvertor(Charset charset) {
		this.charset = charset;
	}

	@Override
	public Mapper toMapper(InputStream in) {
		return new FastjsonMapper(JSON.parseObject(toString(in)));
	}

	@Override
	public Array toArray(InputStream in) {
		return new FastjsonArray(JSON.parseArray(toString(in)));
	}

	@Override
	public String serialize(Object bean) {
		return JSON.toJSONString(bean);
	}

	@Override
	public String serialize(Object bean, String dateFormat) {
		return JSON.toJSONStringWithDateFormat(bean, dateFormat);
	}

	@Override
	public <T> T toBean(Class<T> type, InputStream in) {
		return JSON.parseObject(toString(in), type);
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in) {
		return JSON.parseArray(toString(in), type);
	}

	@Override
	public MsgConvertor getConvertor() {
		return new FastjsonMsgConvertor();
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
