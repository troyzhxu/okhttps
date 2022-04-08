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
public class ListMap<V> extends AbstractMap<String, V> {

    transient final List<Entry<String, V>> entries;

    public ListMap() {
        this(0);
    }

    public ListMap(int initSize) {
        entries = new ArrayList<>(initSize);
    }

    /**
     * 键值对集合类
     */
    class EntrySet extends AbstractSet<Entry<String, V>> {

        @Override
        public Iterator<Entry<String, V>> iterator() {
            return entries.iterator();
        }

        @Override
        public int size() {
            return entries.size();
        }

    }

    transient Set<Entry<String, V>> entrySet;

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
        if (key != null) {
            entries.add(new SimpleEntry<>(key, value));
        }
        return null;
    }

    /**
     * 获取与指定 key 匹配的最后（新）的一个值
     * @param key 键
     * @return 最后（新）的一个值
     */
    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return get((String) key, false);
        }
        return null;
    }

    /**
     * 获取与指定 key 匹配的最后（新）的一个值
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return 匹配 key 的最后（新）的一个值
     */
    public V get(String key, boolean ic) {
        if (key != null) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                Entry<String, V> entry = entries.get(i);
                String k = entry.getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取与指定 key 匹配的所有值列表
     * @param key 键
     * @return List
     */
    public List<V> list(String key) {
        return list(key, false);
    }

    /**
     * 获取与指定 key 匹配的所有值列表
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return List
     */
    public List<V> list(String key, boolean ic) {
        List<V> list = new ArrayList<>();
        if (key != null) {
            for (Entry<String, V> entry : entries) {
                String k = entry.getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    list.add(entry.getValue());
                }
            }
        }
        return list;
    }

    public boolean replace(String key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * 替换与指定 key 匹配的最后（新）的一个值
     * @param key 键
     * @return 被替换的值
     */
    public V replace(String key, V value) {
        return replace(key, value, false);
    }

    /**
     * 替换与指定 key 匹配的最后（新）的一个值
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return 被替换的值
     */
    public V replace(String key, V value, boolean ic) {
        if (key != null) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                Entry<String, V> entry = entries.get(i);
                String k = entry.getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    return entry.setValue(value);
                }
            }
        }
        return null;
    }

    /**
     * 替换与指定 key 匹配的所有值
     * @param key 键
     * @return 被替换的键值对数量
     */
    public int replaceAll(String key, V value) {
        return replaceAll(key, value, false);
    }

    /**
     * 替换与指定 key 匹配的所有值
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return 被替换的键值对数量
     */
    public int replaceAll(String key, V value, boolean ic) {
        int count = 0;
        if (key != null) {
            for (Entry<String, V> entry : entries) {
                String k = entry.getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    entry.setValue(value);
                }
            }
        }
        return count;
    }

    /**
     * 遍历，该方法是为兼容 Android 低版本
     * @param action The action to be performed for each entry
     */
    public void forEach(BiConsumer<? super String, ? super V> action) {
        Platform.forEach(this, action);
    }

    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * 移除与指定 key 匹配的最后（新）一个值
     * @param key 键
     * @return the value was removed
     */
    public V remove(Object key) {
        if (key instanceof String) {
            return remove((String) key, false);
        }
        return null;
    }

    /**
     * 移除与指定 key 匹配的最后（新）一个值
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return the value was removed
     */
    public V remove(String key, boolean ic) {
        if (key != null) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                String k = entries.get(i).getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    return entries.remove(i).getValue();
                }
            }
        }
        return null;
    }

    /**
     * 移除与指定 key 匹配的所有值
     * @param key 键
     * @return the value was removed
     */
    public List<V> removeAll(String key) {
        return removeAll(key, false);
    }

    /**
     * 移除与指定 key 匹配的所有值
     * @param key 键
     * @param ic 匹配 key 时是否忽略大小写
     * @return the value was removed
     */
    public List<V> removeAll(String key, boolean ic) {
        List<V> list = new ArrayList<>();
        if (key != null) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                String k = entries.get(i).getKey();
                if (ic && key.equalsIgnoreCase(k) || !ic && key.equals(k)) {
                    list.add(entries.remove(i).getValue());
                }
            }
        }
        Collections.reverse(list);
        return list;
    }

}
