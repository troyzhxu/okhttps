package cn.zhxu.okhttps.jackson.xml;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.XmlTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor convertor = new JacksonXmlMsgConvertor();
		new XmlTestCases(convertor).run();
	}

}
