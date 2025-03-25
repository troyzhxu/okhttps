package cn.zhxu.okhttps.fastjson;

import cn.zhxu.data.fastjson.FastjsonDataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;

public class FastjsonMsgConvertor extends FastjsonDataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new FastjsonMsgConvertor();
	}

}
