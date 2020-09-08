package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import org.w3c.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlMapper implements Mapper {

    private String[] nameKeys;
    private Element element;

    public XmlMapper(String[] nameKeys, Element element) {
        this.nameKeys = nameKeys;
        this.element = element;
    }

    @Override
    public int size() {
        NamedNodeMap attrs = element.getAttributes();
        return element.getChildNodes().getLength() + attrs.getLength();
    }

    @Override
    public boolean isEmpty() {
        NamedNodeMap attrs = element.getAttributes();
        return attrs.getLength() == 0 && element.getChildNodes().getLength() == 0;
    }

    @Override
    public Mapper getMapper(String key) {
        NodeList nodes = element.getChildNodes();
        Element child = XmlUtils.findElement(nodes, nameKeys, key);
        if (child != null) {
            return new XmlMapper(nameKeys, child);
        }
        return null;
    }

    @Override
    public Array getArray(String key) {
        NodeList nodes = element.getChildNodes();
        List<Element> children = XmlUtils.findElements(nodes, nameKeys, key);
        if (children.size() > 1) {
            return new XmlArray(nameKeys, new NodeList() {
                @Override
                public Node item(int index) {
                    return children.get(index);
                }
                @Override
                public int getLength() {
                    return children.size();
                }
            });
        } else if (children.size() == 1) {
            Element element = children.get(0);
            return new XmlArray(nameKeys, element.getChildNodes());
        }
        return null;
    }

    @Override
    public boolean getBool(String key) {
        String value = XmlUtils.getNodeValue(element, nameKeys, key);
        return XmlUtils.toBoolean(value);
    }

    @Override
    public int getInt(String key) {
        String value = XmlUtils.getNodeValue(element, nameKeys, key);
        return XmlUtils.toInt(value);
    }

    @Override
    public long getLong(String key) {
        String value = XmlUtils.getNodeValue(element, nameKeys, key);
        return XmlUtils.toLong(value);
    }

    @Override
    public float getFloat(String key) {
        String value = XmlUtils.getNodeValue(element, nameKeys, key);
        return XmlUtils.toFloat(value);
    }

    @Override
    public double getDouble(String key) {
        String value = XmlUtils.getNodeValue(element, nameKeys, key);
        return XmlUtils.toDouble(value);
    }

    @Override
    public String getString(String key) {
        return XmlUtils.getNodeValue(element, nameKeys, key);
    }

    @Override
    public boolean has(String key) {
        return XmlUtils.getNodeValue(element, nameKeys, key) != null;
    }

    @Override
    public Set<String> keySet() {
        HashSet<String> set = new HashSet<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            set.add(attr.getName());
        }
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            set.add(node.getNodeName());
            if (node instanceof Element) {
                Element ele = (Element) node;
                for (String nameKey : nameKeys) {
                    String key = ele.getAttribute(nameKey);
                    if (!XmlUtils.isBlank(key)) {
                        set.add(key);
                    }
                }
            }
        }
        return set;
    }

}
