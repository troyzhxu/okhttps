package com.ejlchina.okhttps.test;

import com.ejlchina.okhttps.MsgConvertor;


public class JsonMsgConvertorTest extends BaseMsgConvertorTest {

	public JsonMsgConvertorTest(MsgConvertor msgConvertor) {
		super(msgConvertor);
	}

	@Override
	String getUserObjectStr() {
		return "{\"id\":1,\"name\":\"Jack\"}";
	}

	@Override
	String getUserListStr() {
		return "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
	}
    
}
