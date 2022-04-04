package com.ejlchina.okhttps.jackson;

import com.ejlchina.data.jackson.JacksonDataConvertor;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.MsgConvertor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMsgConvertor extends JacksonDataConvertor implements MsgConvertor, ConvertProvider {

	public JacksonMsgConvertor() { }
	
	public JacksonMsgConvertor(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public String mediaType() {
		return "application/json; charset={charset}";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new JacksonMsgConvertor();
	}

}
