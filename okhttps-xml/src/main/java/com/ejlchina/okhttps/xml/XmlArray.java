package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlArray implements Array {

    private String[] nameKeys;
    private String[] valueKeys;
    private NodeList list;

    public XmlArray(String[] nameKeys, String[] valueKeys, NodeList list) {
        this.nameKeys = nameKeys;
        this.valueKeys = valueKeys;
        this.list = list;
    }

    @Override
    public int size() {
        return list.getLength();
    }

    @Override
    public boolean isEmpty() {
        return list.getLength() == 0;
    }

    @Override
    public Mapper getMapper(int index) {
        if (index < list.getLength()) {
            Node node = list.item(index);
            if (node instanceof Element) {
                return new XmlMapper(nameKeys, valueKeys, (Element) node);
            }
        }
        return null;
    }

    @Override
    public Array getArray(int index) {
        if (index < list.getLength()) {
            Node node = list.item(index);
            return new XmlArray(nameKeys, valueKeys, node.getChildNodes());
        }
        return null;
    }

    @Override
    public boolean getBool(int index) {
        if (index < list.getLength()) {
            String value = XmlUtils.value(list.item(index), valueKeys);
            return XmlUtils.toBoolean(value);
        }
        return false;
    }

    @Override
    public int getInt(int index) {
        if (index < list.getLength()) {
            String value = XmlUtils.value(list.item(index), valueKeys);
            return XmlUtils.toInt(value);
        }
        return 0;
    }

    @Override
    public long getLong(int index) {
        if (index < list.getLength()) {
            String value = XmlUtils.value(list.item(index), valueKeys);
            return XmlUtils.toLong(value);
        }
        return 0;
    }

    @Override
    public float getFloat(int index) {
        if (index < list.getLength()) {
            String value = XmlUtils.value(list.item(index), valueKeys);
            return XmlUtils.toFloat(value);
        }
        return 0;
    }

    @Override
    public double getDouble(int index) {
        if (index < list.getLength()) {
            String value = XmlUtils.value(list.item(index), valueKeys);
            return XmlUtils.toDouble(value);
        }
        return 0;
    }

    @Override
    public String getString(int index) {
        if (index < list.getLength()) {
            return XmlUtils.value(list.item(index), valueKeys);
        }
        return null;
    }
}
