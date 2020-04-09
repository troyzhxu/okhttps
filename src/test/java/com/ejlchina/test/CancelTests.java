package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.internal.HttpClient;
import org.junit.Test;

import java.io.IOException;

/**
 * 测试取消
 */
public class CancelTests extends BaseTest {


    @Test
    public void testCancel() {

        HTTP http = HTTP.builder()
                .baseUrl("http://localhost:8080")
                .build();

        http.async("/user/show/1")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .setOnException((IOException e) -> {
                    println("异常捕获：" + e.getMessage());
                })
                .setOnComplete((HttpResult.State state) -> {
                    println(state);
                })
                .setTag("A")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/2")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .setTag("A.B")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/3")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .setTag("B.C")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/4")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .setTag("C")
                .get();

        println(((HttpClient) http).getTagTaskCount());


        println("标签取消：" + http.cancel("B"));

        println(((HttpClient) http).getTagTaskCount());

        sleep(5000);

        println(((HttpClient) http).getTagTaskCount());

        sleep(5000);

        println(((HttpClient) http).getTagTaskCount());

        sleep(5000);

        println(((HttpClient) http).getTagTaskCount());

//		println("isDone = " + call.isDone());
//		println("isCanceled = " + call.isCanceled());
//
//		println("取消结果 = " + call.cancel());
//
//		println("isDone = " + call.isDone());
//		println("isCanceled = " + call.isCanceled());
//
//		sleep(100);
//		println("++++++++");
//
//		println("isDone = " + call.isDone());
//		println("isCanceled = " + call.isCanceled());
    }

}
