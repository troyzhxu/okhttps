package com.ejlchina.okhttps.internal;

import java.util.*;

/**
 * 一个 Key 可以有多个 Value 的 有序 Map 集合
 * @author troy zhou
 * @since v3.5.0
 * @param <V> 泛型 值
 */
public class MultiValueMap<V> extends AbstractMap<String, V> {


    transient final List<String> keys = new ArrayList<>();

    transient final List<V> values = new ArrayList<>();

    transient Set<Entry<String, V>> entrySet;

    /**
     * 键值对集合类
     */
    class EntrySet extends AbstractSet<Entry<String, V>> {

        @Override
        public Iterator<Entry<String, V>> iterator() {
            Iterator<String> nit = keys.iterator();
            Iterator<V> vit = values.iterator();
            return new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return nit.hasNext() && vit.hasNext();
                }

                @Override
                public Entry<String, V> next() {
                    return new SimpleEntry<>(nit.next(), vit.next());
                }

                @Override
                public void remove() {
                    nit.remove();
                    vit.remove();
                }
            };
        }

        @Override
        public int size() {
            return keys.size();
        }

    }

    /**
     * @return 键值对集合
     */
    @Override
    public Set<Entry<String, V>> entrySet() {
        Set<Map.Entry<String,V>> es = entrySet;
        return es == null ? (entrySet = new EntrySet()) : es;
    }

    /**
     * 向 Map 里放值
     * @param key 键
     * @param value 值
     * @return always null
     */
    @Override
    public V put(String key, V value) {
        // 只存放非空值
        if (key != null && value != null) {
            keys.add(key);
            values.add(value);
        }
        return null;
    }

    /**
     * 获取 Key 对应的最后（新）的一个值
     * @param key 键
     * @return 最后（新）的一个值
     */
    @Override
    public V get(Object key) {
        if (key instanceof String) {
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i).equals(key)) {
                    return values.get(i);
                }
            }
        }
        return null;
    }

    /**
     * 获取 Key 下的所有 Value
     * @param key 键
     * @return List<V>
     */
    public List<V> getValues(String key) {
        List<V> list = new ArrayList<>();
        if (key != null) {
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).equals(key)) {
                    list.add(values.get(i));
                }
            }
        }
        return list;
    }

}
