package com.ejlchina.stomp;

import com.ejlchina.okhttps.test.BaseTest;
import org.junit.Test;

import com.ejlchina.okhttps.OkHttps;

import java.util.concurrent.CountDownLatch;


public class TestCases extends BaseTest {


	static final int COUNT = 100000;
	static final int INTERVAL = 1;

//	@Test
//    public void testA() throws InterruptedException {
//		CountDownLatch latch = new CountDownLatch(COUNT);
//        Stomp.over(OkHttps.webSocket("ws://localhost:8080/ws").heatbeat(10, 10))
//                .setOnConnected(stomp -> {
//                    log("A 已连接");
//                    for (int i = 0; i < COUNT; i++) {
//                    	String message = "Hello : " + i;
//                    	stomp.sendTo("/queue/test", message);
//						log("A 发送：" + message);
//						latch.countDown();
//						sleep(INTERVAL);
//                    }
//                })
//                .setOnDisconnected(c -> log("A 已断开：" + c))
//                .setOnError(msg -> log("A 错误：" + msg))
//                .connect();
//		latch.await();
//    }
//
//	@Test
//    public void testB() throws InterruptedException {
//		CountDownLatch latch = new CountDownLatch(COUNT);
//		Stomp stomp = Stomp.over(OkHttps.webSocket("ws://localhost:8080/ws").heatbeat(10, 10))
//		        .setOnConnected(s -> log("B 已连接"))
//		        .setOnDisconnected(c -> log("B 已断开：" + c))
//		        .setOnError(msg -> log("B 错误：" + msg))
//		        .connect();
//		stomp.queue("/test", msg -> {
//			log("B 接收：" + msg.getPayload());
//			latch.countDown();
//		});
//		latch.await();
//    }

}
