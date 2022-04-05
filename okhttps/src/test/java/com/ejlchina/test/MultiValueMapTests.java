package com.ejlchina.test;

import com.ejlchina.okhttps.Platform;
import com.ejlchina.okhttps.internal.MultiValueMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MultiValueMapTests {


    @Test
    public void test() {
        Map<String, String> m = new HashMap<>();
        m.put("sex", "male");
        m.put("school", "High School");

        Map<String, String> map = new MultiValueMap<>();

        map.put("name", "Jack");
        map.putAll(m);
        map.put("name", "Tom");
        map.put("age", "16");
        map.put("name", "Alice");

        map.putIfAbsent("age1", "17");

        map.clear();

        Platform.forEach(map, (key, value) -> {
            System.out.println(key + " = " + value);
        });
    }


}
