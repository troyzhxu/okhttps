package com.ejlchina.okhttps;

import com.ejlchina.okhttps.test.XmlTestCases;
import org.junit.Test;

public class TestCases {

    @Test
    public void doTest() throws Exception {
        MsgConvertor msgConvertor = new XmlMsgConvertor();
        new XmlTestCases(msgConvertor).run();
    }

}

