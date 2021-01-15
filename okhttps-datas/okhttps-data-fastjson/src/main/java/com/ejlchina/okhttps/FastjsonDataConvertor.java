package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

public class FastjsonDataConvertor implements DataConvertor {

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
		if (object instanceof FastjsonMapper || object instanceof FastjsonArray) {
			return object.toString().getBytes(charset);
		}
		return JSON.toJSONString(object).getBytes(charset);
	}

	@Override
	public <T> T toBean(Type type, InputStream in, Charset charset) {
		try {
			return JSON.parseObject(in, charset, type);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
		return JSON.parseArray(toString(in, charset), type);
	}

	protected String toString(InputStream in, Charset charset) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buff = new byte[512];
		try {
			int len;
			while ((len = in.read(buff)) > 0) {
				output.write(buff, 0, len);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		try {
			return output.toString(charset.name());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

}
