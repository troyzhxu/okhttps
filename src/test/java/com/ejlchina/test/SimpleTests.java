package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import org.junit.Test;

import java.util.List;

public class SimpleTests {


    HTTP http = HTTP.builder()
            .baseUrl("http://localhost:8080")
            .build();

    /**
     * 同步请求示例
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


    void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


class User {

    private int id;
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + "]";
    }

}