package cn.zhxu.okhttps.fastjson2;

import cn.zhxu.data.fastjson2.Fastjson2DataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;

public class Fastjson2MsgConvertor extends Fastjson2DataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new Fastjson2MsgConvertor();
	}

}
