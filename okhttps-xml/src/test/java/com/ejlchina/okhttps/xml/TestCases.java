package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.test.XmlTestCases;
import org.junit.Test;

public class TestCases {

    @Test
    public void doTest() throws Exception {
        MsgConvertor msgConvertor = new XmlMsgConvertor();
        new XmlTestCases(msgConvertor).run();
    }

    @Test
    public void testXml() {

        HTTP http= HTTP.builder()
                .addMsgConvertor(new XmlMsgConvertor())
                .build();

        Mapper mapper = http.sync("https://repo1.maven.org/maven2/com/ejlchina/okhttps/2.4.1/okhttps-2.4.1.pom")
                .get().getBody().toMapper();

        System.out.println(mapper + "\n\n---------\n");

        System.out.println("name = " + mapper.getString("name"));
        System.out.println("artifactId = " + mapper.getString("artifactId"));

        Array dependencies = mapper.getArray("dependencies");

        for (int i = 0; i < dependencies.size(); i++) {
            Mapper dependency = dependencies.getMapper(i);
            String str = dependency.getString("groupId") + ":" + dependency.getString("artifactId");
            System.out.println("依赖" + i + ": " + str);
        }

    }

}

