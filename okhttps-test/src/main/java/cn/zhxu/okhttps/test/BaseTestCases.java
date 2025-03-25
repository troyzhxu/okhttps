package cn.zhxu.okhttps.test;

import cn.zhxu.data.Array;
import cn.zhxu.data.Mapper;
import cn.zhxu.data.TypeRef;
import cn.zhxu.okhttps.HTTP;
import cn.zhxu.okhttps.MsgConvertor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseTestCases {

    final MockWebServer server = new MockWebServer();

    final HTTP http;

    final MsgConvertor msgConvertor;

    final User user1 = new User(1, "Jack");
    final User user2 = new User(1, "Tom");

    public BaseTestCases(MsgConvertor msgConvertor) {
        this.http = HTTP.builder()
                .baseUrl("http://" + server.getHostName() + ":" + server.getPort())
                .addMsgConvertor(msgConvertor)
                .addMsgConvertor(new MsgConvertor.FormConvertor(msgConvertor))
                .build();
        this.msgConvertor = msgConvertor;
    }

    public void run() throws Exception {
        test_01_toMapper();
        test_02_toArray();
        test_03_serializeBean();
        test_04_toBean();
        test_05_toResult();
        test_06_toList();
        test_07_httpOnResponse();
        test_08_httpOnResBody();
        test_09_httpOnResMapper();
        test_10_httpOnResArray();
        test_11_httpOnResBean();
        test_12_httpOnResList();
        test_13_httpOnResString();
        test_14_httpMultiCallback();
        test_15_httpMultiCallback();
        testHttpSync();
    }

    abstract String getUser1Str();

    abstract String getUser1ResultStr();

    abstract String getUserListStr();

    void assertUser1(User user) {
        Assert.assertEquals(1, user.getId());
        Assert.assertEquals("Jack", user.getName());
    }

    void assertUser2(User user) {
        Assert.assertEquals(2, user.getId());
        Assert.assertEquals("Tom", user.getName());
    }

    void assertMapper(Mapper mapper, int id, String name) {
        Assert.assertEquals(2, mapper.size());
        mapper.forEach((key, data) -> {
            if ("id".equals(key)) {
                Assert.assertEquals(id, data.toInt());
            }
            if ("name".equals(key)) {
                Assert.assertEquals(name, data.toString());
            }
        });
        Set<String> keys = mapper.keySet();
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("id"));
        Assert.assertTrue(keys.contains("name"));
        Assert.assertFalse(mapper.isEmpty());
        Assert.assertEquals(2, mapper.size());
        Assert.assertEquals(id, mapper.getInt("id"));
        Assert.assertFalse(mapper.has("age"));
        Assert.assertEquals(0, mapper.getInt("age"));
        Assert.assertEquals(name, mapper.getString("name"));
    }

    void assertMapper1(Mapper mapper) {
        assertMapper(mapper, 1, "Jack");
        assertUser1(mapper.toBean(User.class));
    }

    void assertMapper2(Mapper mapper) {
        assertMapper(mapper, 2, "Tom");
        assertUser2(mapper.toBean(User.class));
    }

    void assertList(List<User> list) {
        Assert.assertEquals(2, list.size());
        assertUser1(list.get(0));
        assertUser2(list.get(1));
    }

    void assertArray(Array array) {
        Assert.assertEquals(2, array.size());
        array.forEach((index, data) -> {
            if (index == 0) {
                assertMapper1(data.toMapper());
            }
            if (index == 1) {
                assertMapper2(data.toMapper());
            }
        });
        assertMapper1(array.getMapper(0));
        assertMapper2(array.getMapper(1));
        assertList(array.toList(User.class));
    }

    void assertUser1Result(Result<User> result) {
        Assert.assertEquals(200, result.getCode());
        Assert.assertEquals("ok", result.getMsg());
        assertUser1(result.getData());
    }

    void test_01_toMapper() {
        InputStream in = new ByteArrayInputStream(getUser1Str().getBytes(StandardCharsets.UTF_8));
        assertMapper1(msgConvertor.toMapper(in, StandardCharsets.UTF_8));
        assertMapper1(msgConvertor.toMapper(getUser1Str()));
        System.out.println("case 01 passed!");
    }

    void test_02_toArray() {
        InputStream in = new ByteArrayInputStream(getUserListStr().getBytes(StandardCharsets.UTF_8));
        assertArray(msgConvertor.toArray(in, StandardCharsets.UTF_8));
        assertArray(msgConvertor.toArray(getUserListStr()));
        System.out.println("case 02 passed!");
    }

    void test_03_serializeBean() {
        byte[] data = msgConvertor.serialize(user1, StandardCharsets.UTF_8);
        Assert.assertEquals(getUser1Str(), new String(data, StandardCharsets.UTF_8));
        Assert.assertEquals(getUser1Str(), msgConvertor.serialize(user1));
        System.out.println("case 03 passed!");
    }

    void test_04_toBean() {
        String json = getUser1Str();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        assertUser1(msgConvertor.toBean(User.class, in, StandardCharsets.UTF_8));
        assertUser1(msgConvertor.toBean(User.class, json));
        System.out.println("case 04 passed!");
    }

    void test_05_toResult() {
        String json = getUser1ResultStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        Type type = new TypeRef<Result<User>>() {}.getType();
        assertUser1Result(msgConvertor.toBean(type, in, StandardCharsets.UTF_8));
        assertUser1Result(msgConvertor.toBean(type, json));
        System.out.println("case 05 passed!");
    }

    void test_06_toList() {
        String json = getUserListStr();
        InputStream in = new ByteArrayInputStream(json.getBytes());
        assertList(msgConvertor.toList(User.class, in, StandardCharsets.UTF_8));
        assertList(msgConvertor.toList(User.class, json));
        System.out.println("case 06 passed!");
    }

    void test_07_httpOnResponse() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUser1Str()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResponse(res -> {
                    assertUser1(res.getBody().toBean(User.class));
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 07 passed!");
    }

    void test_08_httpOnResBody() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUser1Str()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResBody(body -> {
                    assertUser1(body.toBean(User.class));
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 08 passed!");
    }

    void test_09_httpOnResMapper() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUser1Str()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResMapper(mapper -> {
                    assertMapper1(mapper);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 09 passed!");
    }

    void test_10_httpOnResArray() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUserListStr()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResArray(array -> {
                    assertArray(array);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 10 passed!");
    }

    void test_11_httpOnResBean() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUser1Str()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResBean(User.class, user -> {
                    assertUser1(user);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1000, TimeUnit.SECONDS));
        System.out.println("case 11 passed!");
    }

    void test_12_httpOnResList() throws InterruptedException {
        server.enqueue(new MockResponse().setBody(getUserListStr()));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResList(User.class, list -> {
                    assertList(list);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 12 passed!");
    }

    void test_13_httpOnResString() throws InterruptedException {
        String body = getUser1Str();
        server.enqueue(new MockResponse().setBody(body));
        CountDownLatch latch = new CountDownLatch(1);
        http.async("/")
                .setOnResString(str -> {
                    Assert.assertEquals(body, str);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 13 passed!");
    }

    void test_14_httpMultiCallback() throws InterruptedException {
        String user1Str = getUser1Str();
        server.enqueue(new MockResponse().setBody(user1Str));
        CountDownLatch latch = new CountDownLatch(5);
        http.async("/")
                .setOnResponse(res -> {
                    assertUser1(res.getBody().toBean(User.class));
                    latch.countDown();
                })
                .setOnResBody(body -> {
                    assertMapper1(body.toMapper());
                    latch.countDown();
                })
                .setOnResMapper(mapper -> {
                    assertMapper1(mapper);
                    latch.countDown();
                })
                .setOnResBean(User.class, user -> {
                    assertUser1(user);
                    latch.countDown();
                })
                .setOnResString(str -> {
                    Assert.assertEquals(user1Str, str);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 14 passed!");
    }


    void test_15_httpMultiCallback() throws InterruptedException {
        String userListStr = getUserListStr();
        server.enqueue(new MockResponse().setBody(userListStr));
        CountDownLatch latch = new CountDownLatch(5);
        http.async("/")
                .setOnResponse(res -> {
                    assertList(res.getBody().toList(User.class));
                    latch.countDown();
                })
                .setOnResBody(body -> {
                    assertArray(body.toArray());
                    latch.countDown();
                })
                .setOnResList(User.class ,list -> {
                    assertList(list);
                    latch.countDown();
                })
                .setOnResArray(array -> {
                    assertArray(array);
                    latch.countDown();
                })
                .setOnResString(str -> {
                    Assert.assertEquals(userListStr, str);
                    latch.countDown();
                })
                .setOnException(Throwable::printStackTrace)
                .get();
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
        System.out.println("case 15 passed!");
    }

    void testHttpSync() {

    }

}
