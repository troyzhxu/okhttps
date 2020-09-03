package com.ejlchina.stomp;

import java.util.concurrent.TimeUnit;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.internal.WebSocketTask;

public class TestCases {


    public static void main(String[] args) {

    	HTTP http = HTTP.builder()
    			.config(b -> {
    				b.pingInterval(10, TimeUnit.SECONDS);
    			})
    			.build();

        WebSocketTask websocket = http.webSocket("ws://localhost:8080/ws");

        Stomp stomp = Stomp.over(websocket)
                .setOnConnected(s -> {
                    System.out.println("已连接");
                })
                .setOnDisconnected(c -> {
                    System.out.println("已断开：" + c);
                })
                .setOnError(msg -> {
                	System.out.println("错误：" + msg);
                })
                .connect();

//        stomp.topic("/test", msg -> {
//        	System.out.println("收到：" + msg);
//        });
        
        
//        new Timer().schedule(new TimerTask() {
//			
//			@Override
//			public void run() {
//				
//				stomp.sendTo("/topic/test", "Hello World!");
//				
//			}
//		}, 5000);
        
//        new Timer().schedule(new TimerTask() {
//			
//			@Override
//			public void run() {
//				
//				stomp.disconnect();
//				
//			}
//		}, 10000);
    }


}
