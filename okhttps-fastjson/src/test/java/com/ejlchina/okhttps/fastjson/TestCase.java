package com.ejlchina.okhttps.fastjson;

import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new FastjsonMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
