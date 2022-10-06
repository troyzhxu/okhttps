package cn.zhxu.okhttps.fastjson2.jsonb;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new Fastjson2JsonbMsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
