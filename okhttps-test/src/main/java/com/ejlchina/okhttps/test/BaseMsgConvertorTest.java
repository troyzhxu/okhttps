package com.ejlchina.okhttps.test;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.TypeRef;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

public abstract class BaseMsgConvertorTest {

    MsgConvertor msgConvertor;

    public BaseMsgConvertorTest(MsgConvertor msgConvertor) {
        this.msgConvertor = msgConvertor;
    }

    public void run() throws Exception {
        testToMapper();
        testToArray();
        testSerializeBean();
        testSerializeList();
        testSerializeWithDateFormat();
        testToBean();
        testToResult();
        testToList();
    }

    abstract String getUserObjectStr();

    abstract String getResultUserObjectStr();

    abstract String getUserListStr();

    void testToMapper() {
        String json = getUserObjectStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        Mapper mapper = msgConvertor.toMapper(in, StandardCharsets.UTF_8);
        Set<String> keys = mapper.keySet();
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("id"));
        Assert.assertTrue(keys.contains("name"));
        Assert.assertFalse(mapper.isEmpty());
        Assert.assertEquals(2, mapper.size());
        Assert.assertEquals(1, mapper.getInt("id"));
        Assert.assertFalse(mapper.has("age"));
        Assert.assertEquals(0, mapper.getInt("age"));
        Assert.assertEquals("Jack", mapper.getString("name"));
        System.out.println("case 1 passed!");
    }

    void testToArray() {
        String json = getUserListStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        Array jsonObj = msgConvertor.toArray(in, StandardCharsets.UTF_8);
        Assert.assertFalse(jsonObj.isEmpty());
        Assert.assertEquals(2, jsonObj.size());
        Mapper json1 = jsonObj.getMapper(0);
        Mapper json2 = jsonObj.getMapper(1);
        Assert.assertEquals(1, json1.getInt("id"));
        Assert.assertEquals("Jack", json1.getString("name"));
        Assert.assertEquals(2, json2.getInt("id"));
        Assert.assertEquals("Tom", json2.getString("name"));
        System.out.println("case 2 passed!");
    }

    void testSerializeBean() {
        byte[] data = msgConvertor.serialize(new User(1, "Jack"), StandardCharsets.UTF_8);
        String json = new String(data, StandardCharsets.UTF_8);
        Assert.assertEquals(getUserObjectStr(), json);
        System.out.println("case 3 passed!");
    }

    void testSerializeList() {
        User u1 = new User(1, "Jack");
        User u2 = new User(2, "Tom");
        User[] list = new User[] {u1, u2};
        byte[] data = msgConvertor.serialize(list, StandardCharsets.UTF_8);
        String json = new String(data, StandardCharsets.UTF_8);
        Assert.assertEquals(getUserListStr(), json);
        System.out.println("case 4 passed!");
    }

    void testSerializeWithDateFormat() throws ParseException {
        String dataFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
        String date = "2020-05-09 12:30:15";
        byte[] data = msgConvertor.serialize(new DateBean(1, sdf.parse(date)), dataFormat, StandardCharsets.UTF_8);
        String json = new String(data, StandardCharsets.UTF_8);
        Assert.assertTrue(json.contains(date));
        System.out.println("case 5 passed!");
    }

    void testToBean() {
        String json = getUserObjectStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        User user = msgConvertor.toBean(User.class, in, StandardCharsets.UTF_8);
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("Jack", user.getName());
        System.out.println("case 6 passed!");
    }

    void testToResult() {
        String json = getResultUserObjectStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        Result<User> result = msgConvertor.toBean(new TypeRef<Result<User>>(){}.getType(), in, StandardCharsets.UTF_8);
        Assert.assertEquals(200, result.getCode());
        Assert.assertEquals("ok", result.getMsg());
        User user = result.getData();
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("Jack", user.getName());
        System.out.println("case 7 passed!");
    }

    void testToList() {
        String json = getUserListStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        List<User> users = msgConvertor.toList(User.class, in, StandardCharsets.UTF_8);
        Assert.assertEquals(2, users.size());
        User u1 = users.get(0);
        User u2 = users.get(1);
        Assert.assertEquals(1, u1.getId());
        Assert.assertEquals("Jack", u1.getName());
        Assert.assertEquals(2, u2.getId());
        Assert.assertEquals("Tom", u2.getName());
        System.out.println("case 8 passed!");
    }

}
