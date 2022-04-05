package com.ejlchina.okhttps.internal;

import java.util.*;

/**
 * @author troy zhou
 * @since v3.5.0
 * @param <K> 泛型 键
 * @param <V> 泛型 值
 */
public class MultiValueMap<K, V> extends AbstractMap<K, V> {

    private final List<K> names = new ArrayList<>();
    private final List<V> values = new ArrayList<>();

    @Override
    public V put(K key, V value) {
        names.add(key);
        values.add(value);
        return null;
    }

    Set<Entry<K, V>> entrySet;

    class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            Iterator<K> nit = names.iterator();
            Iterator<V> vit = values.iterator();
            return new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return nit.hasNext() && vit.hasNext();
                }

                @Override
                public Entry<K, V> next() {
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
            return names.size();
        }

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        return es == null ? (entrySet = new EntrySet()) : es;
    }

}
