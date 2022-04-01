package com.ejlchina.okhttps.xml;

import com.ejlchina.data.Mapper;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.test.XmlTestCases;
import com.ejlchina.okhttps.xml.XmlMsgConvertor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;

public class TestCases {

    protected MockWebServer server = new MockWebServer();

    protected String mockUrl = "http://" + server.getHostName() + ":" + server.getPort();



    @Test
    public void doTest() throws Exception {
        MsgConvertor msgConvertor = new XmlMsgConvertor();
        new XmlTestCases(msgConvertor).run();
    }

    @Test
    public void test() {
        HTTP http = HTTP.builder()
            .addMsgConvertor(new XmlMsgConvertor())
            .build();

        String xmlstr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><web:CurrAcuRsp xmlns:web=\"http://www.example.org/webservice_zz/\"><CumulRspList><ACCU_NAME>物联网（数据）月流量包定向30元1GB（201604）</ACCU_NAME><CUMULATION_ALREADY>9858</CUMULATION_ALREADY><CUMULATION_LEFT>1038718</CUMULATION_LEFT><CUMULATION_TOTAL>1048576</CUMULATION_TOTAL><END_TIME>20210531000000</END_TIME><OFFER_ID>2038</OFFER_ID><OFFER_NAME>物联网（数据）月流量包定向30元1GB（201604）</OFFER_NAME><START_TIME>20210501000000</START_TIME><UNIT_NAME>KB</UNIT_NAME><OFFER_TYPE>1</OFFER_TYPE><FLOW_ID_GROUP_NBR/><FLOW_ID_GROUP_NAME/></CumulRspList><IRESULT>0</IRESULT><SMSG>处理成功！</SMSG><GROUP_TRANSACTIONID>1000000190202105315663356416</GROUP_TRANSACTIONID><number>1410319993420</number></web:CurrAcuRsp></root>";

        server.enqueue(new MockResponse().setBody(xmlstr));

        Mapper mapper = http.sync(mockUrl).get().getBody().toMapper();

        System.out.println(mapper);
        System.out.println(mapper.getMapper("web:CurrAcuRsp").getMapper("CumulRspList").getString("ACCU_NAME"));
    }

}

