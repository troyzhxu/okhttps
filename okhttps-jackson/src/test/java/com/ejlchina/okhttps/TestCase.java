package com.ejlchina.okhttps;

import com.ejlchina.okhttps.test.JsonMsgConvertorTest;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new JacksonMsgConvertor();
		new JsonMsgConvertorTest(msgConvertor).run();
	}

}
