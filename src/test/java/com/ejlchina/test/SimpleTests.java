package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpResult.Body;
import org.junit.Test;

import java.util.List;

public class SimpleTests extends BaseTest {


    HTTP http = HTTP.builder()
            .baseUrl("http://localhost:8080")
            .build();

    /**
     * 同步请求示例
     * 同步请求直接得到结果，无需设置回调
     */
    @Test
    public void testSync() {
        List<User> users = http.sync("/users")  // http://localhost:8080/users
                .get()                              // GET请求
                .getBody()                          // 获取响应报文体
                .toList(User.class);                // 得到目标数据
        System.out.println("users = " + users);
    }

    /**
     * 异步请求示例
     * 异步请求在OnResponse里接收请求结果
     */
    @Test
    public void testAsync() {
        http.async("/users/1")                //  http://api.demo.com/users/1
                .setOnResponse((HttpResult result) -> {
                    // 得到目标数据
                    User user = result.getBody().toBean(User.class);
                    System.out.println("user = " + user);
                })
                .get();
        sleep(5000);
    }


    /**
     * 启用 cache 示例
     */
    @Test
    public void testCache() {
        Body body = http.sync("/users").get().getBody()
                .cache();   // 启用 cache
        // 使用 cache 后，可以多次使用 toXXX() 方法
        System.out.println(body.toString());
        System.out.println(body.toJsonArray());
        System.out.println(body.toList(User.class));
    }


    /**
     * 获取文件大小示例
     */
    @Test
    public void testContentLength() {
        long t = System.currentTimeMillis();

        long size = http.sync("https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe")
                .get().getBody()
                .close()             // 只是想获得文件大小，不消费报文体，所以直接关闭
                .getContentLength(); // 获得待下载文件的大小

        t = System.currentTimeMillis() - t;

        // 由于未消费报文体，所以本次请求不会消耗下载报文体的时间和网络流量）
        System.out.println("耗时：" + t + " 毫秒");
        System.out.println("size = " + (size / 1024) + " KB");
    }


}


