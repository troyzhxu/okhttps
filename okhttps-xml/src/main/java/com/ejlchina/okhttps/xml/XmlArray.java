package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import org.w3c.dom.NodeList;

public class XmlArray implements Array {

    private NodeList list;

    public XmlArray(NodeList list) {
        this.list = list;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Mapper getMapper(int index) {
        return null;
    }

    @Override
    public Array getArray(int index) {
        return null;
    }

    @Override
    public boolean getBool(int index) {
        return false;
    }

    @Override
    public int getInt(int index) {
        return 0;
    }

    @Override
    public long getLong(int index) {
        return 0;
    }

    @Override
    public float getFloat(int index) {
        return 0;
    }

    @Override
    public double getDouble(int index) {
        return 0;
    }

    @Override
    public String getString(int index) {
        return null;
    }
}
