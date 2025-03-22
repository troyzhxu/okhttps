package cn.zhxu.okhttps.snack3;

import cn.zhxu.data.snack3.Snack3DataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;

public class Snack3MsgConvertor extends Snack3DataConvertor implements MsgConvertor, ConvertProvider {

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new Snack3MsgConvertor();
	}

}














