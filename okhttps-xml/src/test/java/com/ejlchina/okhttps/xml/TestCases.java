package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.test.XmlMsgConvertorTest;
import org.junit.Test;

public class TestCases {

    @Test
    public void doTest() throws Exception {
        MsgConvertor msgConvertor = new XmlMsgConvertor();
        new XmlMsgConvertorTest(msgConvertor).run();
    }


}

