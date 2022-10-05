package cn.zhxu.okhttps.fastjson;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new FastjsonMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
