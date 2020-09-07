package com.ejlchina.okhttps.xml;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TestCases {


    @Test
    public void testXml() throws ParserConfigurationException, IOException, SAXException {

        System.out.println(StandardCharsets.UTF_8.name());
//
//        String xml = "<xml><A value=\"1\">aa</A><B><![CDATA[bbb]]></B>" +
//                "</xml>";
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//
//        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
//
//        System.out.println("document nodeType = " + document.getNodeType());
//
//        Element root = document.getDocumentElement();
//
//        System.out.println("root nodeType = " + root.getNodeType());
//
//        NodeList nodeList = root.getChildNodes();
//
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Node node = nodeList.item(i);
//            System.out.println("nodeType: " + node.getNodeType() + ", NodeName: " + node.getNodeName() +
//                    ", content: " + node.getTextContent() + ", NodeValue: " + node.getNodeValue());
//        }

    }

    public static Map<String, String> parseXml(InputStream input) {
        // 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            // 获取到document里面的全部结点
            NodeList allNodes = document.getFirstChild().getChildNodes();
            Node node;
            Map<String, String> map = new HashMap<>();
            int i = 0;
            while (i < allNodes.getLength()) {
                node = allNodes.item(i);
                if (node instanceof Element) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
                i++;
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("XML解析异常：", e);
        }
    }

}

