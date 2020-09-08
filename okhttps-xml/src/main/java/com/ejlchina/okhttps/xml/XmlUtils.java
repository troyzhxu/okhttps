package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtils {


    public static Element findElement(NodeList nodes, String[] nameKeys, String key) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && nodeNameEquals(node, key)) {
                return (Element) node;
            }
        }
        for (String nameKey : nameKeys) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element && elementKeyEquals((Element) node, nameKey, key)) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    public static List<Element> findElements(NodeList nodes, String[] nameKeys, String key) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && nodeNameEquals(node, key)) {
                elements.add((Element) node);
            }
        }
        if (elements.size() > 0) {
            return elements;
        }
        for (String nameKey : nameKeys) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element && elementKeyEquals((Element) node, nameKey, key)) {
                    elements.add((Element) node);
                }
            }
            if (elements.size() > 0) {
                return elements;
            }
        }
        return elements;
    }

    public static String getNodeValue(Element element, String[] nameKeys, String key) {
        String value = element.getAttribute(key);
        if (!isBlank(value)) {
            return value;
        }
        NodeList children = element.getChildNodes();
        Element ele = findElement(children, nameKeys, key);
        if (ele != null) {
            return value(ele);
        }
        return null;
    }

    private static String value(Node node) {
        String value = ((Element) node).getAttribute("value");
        if (!isBlank(value)) {
            return value;
        }
        return node.getTextContent();
    }

    public static boolean nodeNameEquals(Node node, String name) {
        String nodeName = node.getNodeName();
        return nameEquals(name, nodeName);
    }

    public static boolean elementKeyEquals(Element node, String key, String name) {
        String nodeName = node.getAttribute(key);
        return nameEquals(name, nodeName);
    }

    private static boolean nameEquals(String name, String nodeName) {
        return nodeName.equals(name) || toCamera(nodeName, "-", true).equals(name)
                || toCamera(nodeName, "_", true).equals(name);
    }

    /**
     * 驼峰风格风格转连字符风格
     * @param src
     * @param hyphenation
     * @return
     */
    public static String toHyphenation(String src, String hyphenation) {
        StringBuilder sb = new StringBuilder(src);
        int temp = 0;//定位
        for(int i = 0; i < src.length(); i++){
            if(Character.isUpperCase(src.charAt(i))){
                sb.insert(i + temp, hyphenation);
                temp += 1;
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 连字符风格转驼峰风格
     * @param src 源字符串
     * @param hyphenation 源字符串中的连字符
     * @param initLetterLower 是否首字母小写
     * @return
     */
    public static String toCamera(String src, String hyphenation, boolean initLetterLower) {
        if (isBlank(src)) {
            return src;
        }
        StringBuilder sb = new StringBuilder();
        String[] list = src.split(hyphenation);
        for (int i = 0; i < list.length; i++) {
            if (i == 0 && initLetterLower) {
                sb.append(firstCharToLowerCase(list[i]));
            } else {
                sb.append(firstCharToUpperCase(list[i]));
            }
        }
        return sb.toString();
    }

    /**
     * 首字母变小写
     */
    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     */
    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 字符串为 null 或者内部字符全部为 ' ' '\t' '\n' '\r' 这四类字符时返回 true
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            switch (str.charAt(i)) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    // case '\b':
                    // case '\f':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean toBoolean(String s) {
        return s != null && (s.equalsIgnoreCase("true")
                || s.equalsIgnoreCase("yes")
                || s.equalsIgnoreCase("on")
                || s.equalsIgnoreCase("y")
                || s.equalsIgnoreCase("t"));
    }

    public static int toInt(String s) {
        if (s != null) {
            try {
                s = s.replace(",", "");
                if (s.startsWith("0x")) {
                    return Integer.parseInt(s.substring(2), 16);
                }
                if (s.startsWith("0b")) {
                    return Integer.parseInt(s.substring(2), 2);
                }
                if (s.startsWith("0") && !s.startsWith("0.")) {
                    return Integer.parseInt(s.substring(1), 8);
                }
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                Platform.logError("无法将字符串\"" + s + "\"转换为 Int 数字");
            }
        }
        return 0;
    }

    public static long toLong(String s) {
        if (s != null) {
            try {
                s = s.replace(",", "");
                if (s.startsWith("0x")) {
                    return Long.parseLong(s.substring(2), 16);
                }
                if (s.startsWith("0b")) {
                    return Long.parseLong(s.substring(2), 2);
                }
                if (s.startsWith("0") && !s.startsWith("0.")) {
                    return Long.parseLong(s.substring(1), 8);
                }
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                Platform.logError("无法将字符串\"" + s + "\"转换为 Long 数字");
            }
        }
        return 0;
    }

    public static float toFloat(String s) {
        if (s != null) {
            try {
                return Float.parseFloat(s.replace(",", ""));
            } catch (NumberFormatException e) {
                Platform.logError("无法将字符串\"" + s + "\"转换为 Float 数字");
            }
        }
        return 0;
    }

    public static double toDouble(String s) {
        if (s != null) {
            try {
                return Double.parseDouble(s.replace(",", ""));
            } catch (NumberFormatException e) {
                Platform.logError("无法将字符串\"" + s + "\"转换为 Float 数字");
            }
        }
        return 0;
    }

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
