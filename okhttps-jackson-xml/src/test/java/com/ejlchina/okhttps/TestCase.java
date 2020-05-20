package com.ejlchina.okhttps;

import com.ejlchina.okhttps.test.XmlMsgConvertorTest;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new JacksonXmlMsgConvertor();
		new XmlMsgConvertorTest(msgConvertor).run();
	}
    
}
