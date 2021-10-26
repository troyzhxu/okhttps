package com.ejlchina.test;

import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.AbstractHttpImpl;
import org.junit.Test;

import java.io.IOException;

/**
 * 测试取消
 */
public class CancelTests extends BaseTest {


	@Test
    public void testCancelByTag() {
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
        http.async("/user/show/1")
                .setOnResponse(BaseTest::println)
                .setOnException((IOException e) -> {
                    println("异常捕获：" + e.getMessage());
                })
                .setOnComplete(BaseTest::println)
                .tag("A")
                .get();

        println(((AbstractHttpImpl) http).getTagTaskCount());

        http.async("/user/show/2")
                .setOnResponse(BaseTest::println)
                .tag("A.B")
                .get();

        println(((AbstractHttpImpl) http).getTagTaskCount());

        http.async("/user/show/3")
                .setOnResponse(BaseTest::println)
                .tag("B.C")
                .get();

        println(((AbstractHttpImpl) http).getTagTaskCount());

        http.async("/user/show/4")
                .setOnResponse(BaseTest::println)
                .tag("C")
                .get();

        println(((AbstractHttpImpl) http).getTagTaskCount());

        println("标签取消：" + http.cancel("B"));

        println(((AbstractHttpImpl) http).getTagTaskCount());

        sleep(5000);

        println(((AbstractHttpImpl) http).getTagTaskCount());

        sleep(5000);

        println(((AbstractHttpImpl) http).getTagTaskCount());

        sleep(5000);

        println(((AbstractHttpImpl) http).getTagTaskCount());
    }

}
