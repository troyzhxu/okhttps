package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import org.w3c.dom.Element;

import java.util.Set;

public class XmlMapper implements Mapper {

    private Element element;

    public XmlMapper(Element element) {
        this.element = element;
    }

    @Override
    public int size() {
        return element.getChildNodes().getLength();
    }

    @Override
    public boolean isEmpty() {
        return element.getChildNodes().getLength() == 0;
    }

    @Override
    public Mapper getMapper(String key) {

        return null;
    }

    @Override
    public Array getArray(String key) {
        return null;
    }

    @Override
    public boolean getBool(String key) {
        return false;
    }

    @Override
    public int getInt(String key) {
        return 0;
    }

    @Override
    public long getLong(String key) {
        return 0;
    }

    @Override
    public float getFloat(String key) {
        return 0;
    }

    @Override
    public double getDouble(String key) {
        return 0;
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public Set<String> keySet() {
        return null;
    }
}
