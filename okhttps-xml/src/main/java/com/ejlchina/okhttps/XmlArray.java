package com.ejlchina.okhttps;

import org.w3c.dom.Element;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class XmlArray implements Array {

    private final String[] nameKeys;
    private final String[] valueKeys;
    private final List<Element> list;

    public XmlArray(String[] nameKeys, String[] valueKeys, List<Element> list) {
        this.nameKeys = nameKeys;
        this.valueKeys = valueKeys;
        this.list = list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Mapper getMapper(int index) {
        if (index < list.size()) {
            return new XmlMapper(nameKeys, valueKeys, list.get(index));
        }
        return null;
    }

    @Override
    public Array getArray(int index) {
        if (index < list.size()) {
            Element node = list.get(index);
            return new XmlArray(nameKeys, valueKeys, XmlUtils.children(node));
        }
        return null;
    }

    @Override
    public boolean getBool(int index) {
        if (index < list.size()) {
            String value = XmlUtils.value(list.get(index), valueKeys);
            return XmlUtils.toBoolean(value);
        }
        return false;
    }

    @Override
    public int getInt(int index) {
        if (index < list.size()) {
            String value = XmlUtils.value(list.get(index), valueKeys);
            return XmlUtils.toInt(value);
        }
        return 0;
    }

    @Override
    public long getLong(int index) {
        if (index < list.size()) {
            String value = XmlUtils.value(list.get(index), valueKeys);
            return XmlUtils.toLong(value);
        }
        return 0;
    }

    @Override
    public float getFloat(int index) {
        if (index < list.size()) {
            String value = XmlUtils.value(list.get(index), valueKeys);
            return XmlUtils.toFloat(value);
        }
        return 0;
    }

    @Override
    public double getDouble(int index) {
        if (index < list.size()) {
            String value = XmlUtils.value(list.get(index), valueKeys);
            return XmlUtils.toDouble(value);
        }
        return 0;
    }

    @Override
    public String getString(int index) {
        if (index < list.size()) {
            return XmlUtils.value(list.get(index), valueKeys);
        }
        return null;
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < list.size(); i++) {
                TransformerFactory.newInstance().newTransformer()
                        .transform(new DOMSource(list.get(i)), new StreamResult(baos));
                if (i < list.size() - 1) {
                    baos.write('\n');
                }
            }
            return baos.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
