package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.Process;
import org.junit.Test;

import java.util.List;

public class JsonTests extends BaseTest {

    @Test
    public void syncJsonExample() {
        HTTP http = HTTP.builder().build();
        // 同步请求
        HttpResult result = http.sync("/user/save")
                .addJsonParam("name", "Tom")
                .addJsonParam("age", 23)
                .post();

        println("result = " + result);

        result = http.sync("/user/show/1").get();

        println("result = " + result);

        println("isSuccessful = " + result.isSuccessful());
    }


    @Test
    public void testToList() {
        long t0 = System.currentTimeMillis();

        HTTP http = HTTP.builder()
                .baseUrl("http://xxx.cdyun.vip/ejlchina")
                .build();

        HttpResult result = http.sync("/comm/provinces")
                .setRange(0)
                .get();

        println(t0, "status: " + result.getStatus());
        println();
        println(t0, "headers: " + result.getHeaders());
        println();
        HttpResult.Body body = result.getBody();

        println(t0, "total: " + body.getContentLength());
        println();
        List<User> list = body.setStepRate(0.1)
                .setOnProcess((Process process) -> {
                    println(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .toList(User.class);

        println(t0, list.toString());
    }


}
