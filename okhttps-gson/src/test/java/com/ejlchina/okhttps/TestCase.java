package com.ejlchina.okhttps;

import com.ejlchina.okhttps.test.JsonMsgConvertorTest;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new GsonMsgConvertor();
		new JsonMsgConvertorTest(msgConvertor).run();
	}

}
