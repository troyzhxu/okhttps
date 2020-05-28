package com.ejlchina.okhttps.test;

import com.ejlchina.okhttps.MsgConvertor;


public class XmlMsgConvertorTest extends BaseMsgConvertorTest {

	public XmlMsgConvertorTest(MsgConvertor msgConvertor) {
		super(msgConvertor);
	}

	@Override
	String getUserObjectStr() {
		return "<User><id>1</id><name>Jack</name></User>";
	}

	@Override
	String getUserListStr() {
		return "<ArrayList><item><id>1</id><name>Jack</name></item><item><id>2</id><name>Tom</name></item></ArrayList>";
	}
    
}
