package com.ejlchina.okhttps;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XmlUtils {


    public static List<Element> children(Element element) {
        NodeList nodes = element.getChildNodes();
        List<Element> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element)  {
                list.add((Element) node);
            }
        }
        return list;
    }

    public static Element findElement(List<Element> nodes, String[] nameKeys, String key) {
        for (Element node : nodes) {
            if (nodeNameEquals(node, key)) {
                return node;
            }
        }
        for (String nameKey : nameKeys) {
            for (Element node : nodes) {
                if (elementKeyEquals(node, nameKey, key)) {
                    return node;
                }
            }
        }
        return null;
    }

    public static List<Element> findElements(List<Element> nodes, String[] nameKeys, String key) {
        List<Element> elements = new ArrayList<>();
        for (Element node : nodes) {
            if (nodeNameEquals(node, key)) {
                elements.add(node);
            }
        }
        if (elements.size() > 0) {
            return elements;
        }
        for (String nameKey : nameKeys) {
            for (Element node : nodes) {
                if (elementKeyEquals(node, nameKey, key)) {
                    elements.add(node);
                }
            }
            if (elements.size() > 0) {
                return elements;
            }
        }
        return elements;
    }

    public static String value(Element node, String[] valueKeys) {
        for (String valueKey : valueKeys) {
            String value = node.getAttribute(valueKey);
            if (!isBlank(value)) {
                return value;
            }
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
     * 连字符风格转驼峰风格
     * @param src 源字符串
     * @param hyphenation 源字符串中的连字符
     * @param initLetterLower 是否首字母小写
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
                throw new IllegalStateException("无法将字符串\"" + s + "\"转换为 Int 数字", e);
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
                throw new IllegalStateException("无法将字符串\"" + s + "\"转换为 Long 数字", e);
            }
        }
        return 0;
    }

    public static float toFloat(String s) {
        if (s != null) {
            try {
                return Float.parseFloat(s.replace(",", ""));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("无法将字符串\"" + s + "\"转换为 Float 数字", e);
            }
        }
        return 0;
    }

    public static double toDouble(String s) {
        if (s != null) {
            try {
                return Double.parseDouble(s.replace(",", ""));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("无法将字符串\"" + s + "\"转换为 Float 数字", e);
            }
        }
        return 0;
    }

}
