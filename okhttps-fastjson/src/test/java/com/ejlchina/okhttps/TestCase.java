package com.ejlchina.okhttps;

import com.ejlchina.okhttps.fastjson.FastjsonMsgConvertor;
import com.ejlchina.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new FastjsonMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
