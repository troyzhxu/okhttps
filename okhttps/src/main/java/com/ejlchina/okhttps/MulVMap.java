package com.ejlchina.okhttps;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * 一个 Key 可以有多个 Value 的 有序 Map 集合
 * 本类设计作为 HTTP 请求参数 的容器
 * 由于请求参数一般都不会太多，且很少有删除操作，所以本类内部使用 {@link ArrayList } 实现
 * @author troy zhou
 * @since v3.5.0
 * @param <V> 泛型 值
 */
public class MulVMap<V> extends AbstractMap<String, V> {

    transient final List<String> keys;

    transient final List<V> values;

    transient Set<Entry<String, V>> entrySet;

    public MulVMap() {
        this(0);
    }

    public MulVMap(int initSize) {
        keys = new ArrayList<>(initSize);
        values = new ArrayList<>(initSize);
    }

    static class Itr<V> implements Iterator<Entry<String, V>> {

        final Iterator<String> kit;
        final Iterator<V> vit;

        public Itr(Iterator<String> kit, Iterator<V> vit) {
            this.kit = kit;
            this.vit = vit;
        }

        @Override
        public boolean hasNext() {
            return kit.hasNext() && vit.hasNext();
        }

        @Override
        public Entry<String, V> next() {
            return new SimpleEntry<>(kit.next(), vit.next());
        }

        @Override
        public void remove() {
            kit.remove();
            vit.remove();
        }

    }

    /**
     * 键值对集合类
     */
    class EntrySet extends AbstractSet<Entry<String, V>> {

        @Override
        public Iterator<Entry<String, V>> iterator() {
            return new Itr<>(keys.iterator(), values.iterator());
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
    public List<V> getAll(String key) {
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

    /**
     * 遍历，该方法是为兼容 Android 低版本
     * @param action The action to be performed for each entry
     */
    public void forEach(BiConsumer<? super String, ? super V> action) {
        Platform.forEach(this, action);
    }

    /**
     * 移除指定 键 和 值 的所有键值对
     * @param key 键
     * @param value 值
     * @return true if the value was removed
     */
    public boolean remove(Object key, Object value) {
        boolean removed = false;
        if (key instanceof String && value != null) {
            Iterator<Entry<String, V>> it = entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, V> e = it.next();
                String k = e.getKey();
                V v = e.getValue();
                if (k.equals(key) && v.equals(value)) {
                    it.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    /**
     * 移除指定 指定键 的最后（新）一个值
     * @param key 键
     * @return the value was removed
     */
    public V remove(Object key) {
        if (key instanceof String) {
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i).equals(key)) {
                    keys.remove(i);
                    return values.remove(i);
                }
            }
        }
        return null;
    }

    /**
     * 移除指定 指定键 的 所有值
     * @param key 键
     * @return the value was removed
     */
    public List<V> removeAll(String key) {
        List<V> list = new ArrayList<>();
        if (key != null) {
            for (int i = keys.size() - 1; i >= 0; i--) {
                if (keys.get(i).equals(key)) {
                    list.add(values.remove(i));
                    keys.remove(i);
                }
            }
        }
        Collections.reverse(list);
        return list;
    }

}
