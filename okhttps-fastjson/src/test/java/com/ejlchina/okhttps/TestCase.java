package com.ejlchina.okhttps;

import com.ejlchina.okhttps.test.MsgConvertorTest;
import org.junit.Test;


public class TestCase {


	@Test
	public void doTest() throws Exception {
		MsgConvertor msgConvertor = new FastjsonMsgConvertor();
		new MsgConvertorTest(msgConvertor).run();
	}

    
}
