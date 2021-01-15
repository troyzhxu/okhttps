package com.ejlchina.okhttps;

import java.util.HashMap;

/**
 * @author Troy Zhou
 * @since v2.5.2
 */
public class HashMapper extends HashMap<String, Object> implements Mapper {

    @Override
    public Mapper getMapper(String key) {
        Object o = get(key);
        if (o instanceof Mapper) {
            return (Mapper) o;
        }
        return null;
    }

    @Override
    public Array getArray(String key) {
        Object o = get(key);
        if (o instanceof Array) {
            return (Array) o;
        }
        return null;
    }

    @Override
    public boolean getBool(String key) {
        Object o = get(key);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return false;
    }

    @Override
    public int getInt(String key) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return 0;
    }

    @Override
    public long getLong(String key) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        return 0;
    }

    @Override
    public float getFloat(String key) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return 0;
    }

    @Override
    public double getDouble(String key) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return 0;
    }

    @Override
    public String getString(String key) {
        Object o = get(key);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    @Override
    public boolean has(String key) {
        return containsKey(key);
    }

}
