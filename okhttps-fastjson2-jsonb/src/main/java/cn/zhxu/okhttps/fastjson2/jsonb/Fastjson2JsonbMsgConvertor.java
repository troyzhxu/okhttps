package cn.zhxu.okhttps.fastjson2.jsonb;

import cn.zhxu.data.fastjson2.jsonb.Fastjson2JsonbDataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;

public class Fastjson2JsonbMsgConvertor extends Fastjson2JsonbDataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/jsonb";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new Fastjson2JsonbMsgConvertor();
	}

}
