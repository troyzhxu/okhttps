package cn.zhxu.okhttps.fastjson;

import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.OkHttpsException;
import com.alibaba.fastjson.JSON;
import cn.zhxu.data.fastjson.FastjsonDataConvertor;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class FastjsonMsgConvertor extends FastjsonDataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json";
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
			throw new OkHttpsException("读取文本异常", e);
		}
	}

}
