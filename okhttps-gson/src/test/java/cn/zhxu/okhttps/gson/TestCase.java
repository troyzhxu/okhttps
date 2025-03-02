package cn.zhxu.okhttps.gson;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new GsonMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
