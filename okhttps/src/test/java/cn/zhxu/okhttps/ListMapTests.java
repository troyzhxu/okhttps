package cn.zhxu.okhttps;

import cn.zhxu.data.ArrayListMap;
import cn.zhxu.data.LinkedListMap;
import cn.zhxu.data.ListMap;
import org.junit.Test;

public class ListMapTests {

    @Test
    public void map() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            ListMap<Object> map = new ArrayListMap<>();
            map.put("name", "jack");
            map.put("school", "High School");
            map.put("age", 20);
            map.put("sex", "Male");
            map.put("height", 170);
            map.put("type", 2);
            map.put("timestamp", "1234567890");
            map.put("name", "jack");
            map.put("school", "High School");
            map.put("age", 20);
            map.put("sex", "Male");
            map.put("height", 170);
            map.put("type", 2);
            map.put("timestamp", "1234567890");
            map.forEach((key, value) -> { });
        }
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            ListMap<Object> map = new LinkedListMap<>();
            map.put("name", "jack");
            map.put("school", "High School");
            map.put("age", 20);
            map.put("sex", "Male");
            map.put("height", 170);
            map.put("type", 2);
            map.put("timestamp", "1234567890");
            map.put("name", "jack");
            map.put("school", "High School");
            map.put("age", 20);
            map.put("sex", "Male");
            map.put("height", 170);
            map.put("type", 2);
            map.put("timestamp", "1234567890");
            map.forEach((key, value) -> { });
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(t2 - t1);
    }

}
