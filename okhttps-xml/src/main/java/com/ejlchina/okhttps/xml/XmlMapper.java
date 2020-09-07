package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;

import java.util.Set;

public class XmlMapper implements Mapper {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
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
