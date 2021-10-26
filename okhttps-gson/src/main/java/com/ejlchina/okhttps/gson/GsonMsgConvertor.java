package com.ejlchina.okhttps.gson;

import com.ejlchina.data.gson.GsonDataConvertor;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.MsgConvertor;
import com.google.gson.Gson;

public class GsonMsgConvertor extends GsonDataConvertor implements MsgConvertor, ConvertProvider {

	public GsonMsgConvertor() { }
	
	public GsonMsgConvertor(Gson gson) {
		super(gson);
	}

	@Override
	public String mediaType() {
		return "application/json; charset={charset}";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new GsonMsgConvertor();
	}

}














