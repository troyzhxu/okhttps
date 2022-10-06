package cn.zhxu.okhttps.snack3;

import cn.zhxu.okhttps.MsgConvertor;
import cn.zhxu.okhttps.test.JsonTestCases;
import org.junit.Test;


public class TestCase {

	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new Snack3MsgConvertor();
		new JsonTestCases(msgConvertor).run();
	}

}
