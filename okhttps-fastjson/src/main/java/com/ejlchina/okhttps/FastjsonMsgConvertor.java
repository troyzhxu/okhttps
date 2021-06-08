package com.ejlchina.okhttps;

import com.alibaba.fastjson.JSON;
import com.ejlchina.data.FastjsonDataConvertor;
import com.ejlchina.okhttps.internal.HttpException;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class FastjsonMsgConvertor extends FastjsonDataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json; charset={charset}";
	}

	@Override
	public <T> T toBean(Type type, InputStream in, Charset charset) {
		return JSON.parseObject(toString(in, charset), type);
	}

	@Override
	public MsgConvertor getConvertor() {
		return new FastjsonMsgConvertor();
	}

	@Override
	protected String toString(InputStream in, Charset charset) {
		try {
			return Okio.buffer(Okio.source(in)).readString(charset);
		} catch (IOException e) {
			throw new HttpException("读取文本异常", e);
		}
	}

}
