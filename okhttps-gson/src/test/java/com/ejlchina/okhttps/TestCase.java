package com.ejlchina.okhttps;

import com.ejlchina.okhttps.gson.GsonMsgConvertor;
import com.ejlchina.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new GsonMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
