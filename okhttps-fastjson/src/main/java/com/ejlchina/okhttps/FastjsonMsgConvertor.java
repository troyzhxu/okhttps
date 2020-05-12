package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.internal.HttpException;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class FastjsonMsgConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public Mapper toMapper(InputStream in, Charset charset) {
		return new FastjsonMapper(JSON.parseObject(toString(in, charset)));
	}

	@Override
	public Array toArray(InputStream in, Charset charset) {
		return new FastjsonArray(JSON.parseArray(toString(in, charset)));
	}

	@Override
	public byte[] serialize(Object object, Charset charset) {
		return serialize(object, null, charset);
	}

	@Override
	public byte[] serialize(Object object, String dateFormat, Charset charset) {
		if (dateFormat != null) {
			return JSON.toJSONStringWithDateFormat(object, dateFormat).getBytes(charset);
		}
		return JSON.toJSONString(object).getBytes(charset);
	}

	@Override
	public <T> T toBean(Class<T> type, InputStream in, Charset charset) {
		return JSON.parseObject(toString(in, charset), type);
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
		return JSON.parseArray(toString(in, charset), type);
	}

	@Override
	public MsgConvertor getConvertor() {
		return new FastjsonMsgConvertor();
	}

	private String toString(InputStream in, Charset charset) {
		try {
			return Okio.buffer(Okio.source(in)).readString(charset);
		} catch (IOException e) {
			throw new HttpException("读取文本异常", e);
		}
	}

}
