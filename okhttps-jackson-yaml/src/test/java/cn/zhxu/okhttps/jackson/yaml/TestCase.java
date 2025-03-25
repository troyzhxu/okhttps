package cn.zhxu.okhttps.jackson.yaml;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor convertor = new JacksonYamlMsgConvertor();
//		new JsonTestCases(convertor).run();
	}

}
