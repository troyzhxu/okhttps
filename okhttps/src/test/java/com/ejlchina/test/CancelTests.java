package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.HttpClient;
import com.ejlchina.okhttps.test.BaseTest;
import org.junit.Test;

import java.io.IOException;

/**
 * 测试取消
 */
public class CancelTests extends BaseTest {


	@Test
    public void testCancelByTag() {
        HTTP http = HTTP.builder()
                .baseUrl("http://tst-api-mini.cdyun.vip")
                .build();

        new Thread(() -> {
        	sleep(50);
//        	http.cancelAll();
//        	int count = http.cancel("A");
//        	println("count = " + count);
        }).start();
        
        http.async("/ejlchina/comm/provinces")
        	.tag("A")
        	.setOnResponse((HttpResult result) -> {
        		println("异步：result = " + result);
        	})
        	.setOnComplete((State state) -> {
        		println("异步：state = " + state);
        	})
        	.get();
        
        try {
	        HttpResult result = http.sync("/ejlchina/comm/provinces")
	        		.tag("A")
	        		.get();
	        
	        println("同步：result = " + result);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        sleep(5000);
    }

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
                .tag("A")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/2")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .tag("A.B")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/3")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .tag("B.C")
                .get();

        println(((HttpClient) http).getTagTaskCount());

        http.async("/user/show/4")
                .setOnResponse((HttpResult result) -> {
                    println(result);
                })
                .tag("C")
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
