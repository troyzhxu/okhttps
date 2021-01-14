package com.ejlchina.okhttps;

import org.w3c.dom.*;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlMapper implements Mapper {

    final private String[] nameKeys;
    final private String[] valueKeys;
    final private Element element;
    private List<Element> children;

    public XmlMapper(String[] nameKeys, String[] valueKeys, Element element) {
        this.nameKeys = nameKeys;
        this.valueKeys = valueKeys;
        this.element = element;
    }

    @Override
    public int size() {
        NamedNodeMap attrs = element.getAttributes();
        return children().size() + attrs.getLength();
    }

    @Override
    public boolean isEmpty() {
        NamedNodeMap attrs = element.getAttributes();
        return attrs.getLength() == 0 && children().size() == 0;
    }

    @Override
    public Mapper getMapper(String key) {
        Element child = XmlUtils.findElement(children(), nameKeys, key);
        if (child != null) {
            return new XmlMapper(nameKeys, valueKeys, child);
        }
        return null;
    }

    @Override
    public Array getArray(String key) {
        List<Element> children = XmlUtils.findElements(children(), nameKeys, key);
        if (children.size() > 1) {
            return new XmlArray(nameKeys, valueKeys, children);
        } else if (children.size() == 1) {
            Element element = children.get(0);
            return new XmlArray(nameKeys, valueKeys, XmlUtils.children(element));
        }
        return null;
    }

    @Override
    public boolean getBool(String key) {
        return XmlUtils.toBoolean(getString(key));
    }

    @Override
    public int getInt(String key) {
        return XmlUtils.toInt(getString(key));
    }

    @Override
    public long getLong(String key) {
        return XmlUtils.toLong(getString(key));
    }

    @Override
    public float getFloat(String key) {
        return XmlUtils.toFloat(getString(key));
    }

    @Override
    public double getDouble(String key) {
        return XmlUtils.toDouble(getString(key));
    }

    @Override
    public String getString(String key) {
        String value = element.getAttribute(key);
        if (!XmlUtils.isBlank(value)) {
            return value;
        }
        Element ele = XmlUtils.findElement(children(), nameKeys, key);
        if (ele != null) {
            return XmlUtils.value(ele, valueKeys);
        }
        return null;
    }

    @Override
    public boolean has(String key) {
        return getString(key) != null;
    }

    @Override
    public Set<String> keySet() {
        HashSet<String> set = new HashSet<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            set.add(attr.getName());
        }
        List<Element> nodes = children();
        for (Element node : nodes) {
            set.add(node.getNodeName());
            for (String nameKey : nameKeys) {
                String key = node.getAttribute(nameKey);
                if (!XmlUtils.isBlank(key)) {
                    set.add(key);
                }
            }
        }
        return set;
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TransformerFactory.newInstance().newTransformer()
                    .transform(new DOMSource(element), new StreamResult(baos));
            return baos.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected List<Element> children() {
        synchronized (element) {
            if (children == null) {
                children = XmlUtils.children(element);
            }
        }
        return children;
    }


}
