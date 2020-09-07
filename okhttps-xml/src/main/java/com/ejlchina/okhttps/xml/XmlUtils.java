package com.ejlchina.okhttps.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class XmlUtils {


    public static String toXml(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (String name : params.keySet()) {
            String value = params.get(name);
            sb.append("<").append(name).append(">");
            sb.append(value);
            sb.append("</").append(name).append(">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    public static Map<String, String> parseXml(String input) {
        // 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
        return parseXml(getStringStream(input));
    }

    public static Map<String, String> parseXml(InputStream input) {
        // 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            // 获取到document里面的全部结点
            NodeList allNodes = document.getFirstChild().getChildNodes();
            Map<String, String> map = new HashMap<>();
            int i = 0;
            while (i < allNodes.getLength()) {
                Node node = allNodes.item(i);
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

    public static InputStream getStringStream(String input) {
        try {
            return new ByteArrayInputStream(input != null ? input.getBytes("UTF-8") : new byte[0]);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("解码出错", e);
        }
    }

}
