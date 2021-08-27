package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Test;

public class SimpleTests extends BaseTest {


	MockWebServer server = new MockWebServer();
	
    HTTP http = HTTP.builder()
            .baseUrl("http://" + server.getHostName() + ":" + server.getPort())
            .build();

    @Test
    public void testSyncPost() throws InterruptedException {
        http.async("/").bodyType(OkHttps.JSON).post();
        Assert.assertEquals(server.takeRequest().getHeader("Content-Type"), "application/json; charset=UTF-8");
        http.async("/").bodyType(OkHttps.FORM).post();
        Assert.assertEquals(server.takeRequest().getHeader("Content-Type"), "application/x-www-form-urlencoded; charset=UTF-8");
    }


    /**
     * 同步请求示例
     * 同步请求直接得到结果，无需设置回调
     */
    @Test
    public void testSyncToString() {
    	server.enqueue(new MockResponse().setBody("Hello World!"));
        String hello = http.sync("/users")  // http://localhost:8080/users
                .get()                              // GET请求
                .getBody()                          // 获取响应报文体
                .toString();                // 得到目标数据
        Assert.assertEquals(hello, "Hello World!");
    }

    @Test
    public void testUserAgent() throws InterruptedException {
        http.async("/user").addHeader("User-Agent", "123456").get();
        String userAgent = server.takeRequest().getHeader("User-Agent");
        Assert.assertEquals("123456", userAgent);
    }

    /**
     * 同步请求示例
     * 同步请求直接得到结果，无需设置回调
     */
//    @Test
//    public void testSyncToList() {
//    	User u1 = new User(1, "Jack");
//    	User u2 = new User(2, "Tom");
//    	List<User> list = Arrays.asList(u1, u2);
//    	server.enqueue(new MockResponse().setBody(JSON.toJSONString(list)));
//
//        List<User> users = http.sync("/users")  // http://localhost:8080/users
//                .get()                              // GET请求
//                .getBody()                          // 获取响应报文体
//                .toList(User.class);                // 得到目标数据
//
//        assertEquals(u1, users.get(0));
//        assertEquals(u2, users.get(1));
//    }
//
//    /**
//     * 异步请求示例
//     * 异步请求在OnResponse里接收请求结果
//     */
//    @Test
//    public void testAsync() {
//        http.async("/users/1")                //  http://api.demo.com/users/1
//                .setOnResponse((HttpResult result) -> {
//                    // 得到目标数据
//                    User user = result.getBody().toBean(User.class);
//                    System.out.println("user = " + user);
//                })
//                .get();
//        sleep(5000);
//    }

    /**
     * 启用 cache 示例
     */
    @Test
    public void testCache() {
        String content = "test cache method";
        server.enqueue(new MockResponse().setBody(content));
        HttpResult.Body body = http.sync("/users").get().getBody()
                .cache();   // 启用 cache
        // 使用 cache 后，可以多次使用 toXXX() 方法
        Assert.assertEquals(content, body.toString());
        Assert.assertEquals(content, body.toString());
    }

}


