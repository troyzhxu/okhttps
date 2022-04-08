package com.ejlchina.test;

import com.ejlchina.okhttps.Platform;
import com.ejlchina.okhttps.ListMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ListMapTests {

    @Test
    public void test1() {
        Map<String, String> m = new HashMap<>();
        m.put("sex", "male");
        m.put("school", "School");

        Map<String, String> map = new ListMap<>();
        map.putAll(m);

        Assert.assertEquals("male", map.get("sex"));
        Assert.assertEquals("School", map.get("school"));
        Assert.assertEquals(2, map.size());
    }

    @Test
    public void test2() {
        ListMap<Object> map = new ListMap<>();
        map.put("name", "Jack");
        map.put("age", 25);
        map.put("name", "Tom");
        map.put("age", 30);
        map.put("name", "Alice");
        map.put("age", 26);

        Assert.assertEquals(6, map.size());
        Assert.assertEquals("Alice", map.get("name"));
        Assert.assertEquals(26, map.get("age"));

        String[] NAMES = new String[] {
                "Jack", "Tom", "Alice"
        };
        int[] AGES = new int[] {
                25, 30, 26
        };

        List<Object> names = map.list("name");
        Assert.assertEquals(3, names.size());
        Assert.assertEquals(NAMES[0], names.get(0));
        Assert.assertEquals(NAMES[1], names.get(1));
        Assert.assertEquals(NAMES[2], names.get(2));

        List<Object> ages = map.list("age");
        Assert.assertEquals(3, ages.size());
        Assert.assertEquals(AGES[0], ages.get(0));
        Assert.assertEquals(AGES[1], ages.get(1));
        Assert.assertEquals(AGES[2], ages.get(2));

        AtomicInteger index = new AtomicInteger(0);

        Platform.forEach(map, (key, value) -> {
            int i = index.getAndIncrement();
            if (i % 2 == 1) {
                Assert.assertEquals("age", key);
                Assert.assertEquals(AGES[i / 2], value);
            } else {
                Assert.assertEquals("name", key);
                Assert.assertEquals(NAMES[i / 2], value);
            }
        });
    }


    @Test
    public void test3() {
        ListMap<String> map = new ListMap<>();
        map.put("name", "Jack");
        map.put("name", "Ketty");
        map.put("name", "Tom");
        map.put("name", "Tom");
        map.put("name", "Alice");
        map.put("age", "30");

        Assert.assertEquals(6, map.size());
        Assert.assertEquals("Alice", map.get("name"));
        Assert.assertEquals("30", map.get("age"));

        Assert.assertTrue(map.remove("name", "Tom"));

        Assert.assertEquals(4, map.size());
        Assert.assertEquals("Alice", map.get("name"));

        Assert.assertEquals("Alice", map.remove("name"));

        Assert.assertEquals(3, map.size());
        Assert.assertEquals("Ketty", map.get("name"));

        Assert.assertArrayEquals(new String[] {"Jack", "Ketty"}, map.removeAll("name").toArray());

        Assert.assertEquals(1, map.size());
        Assert.assertEquals("30", map.get("age"));
        Assert.assertNull(map.get("name"));
    }

}
