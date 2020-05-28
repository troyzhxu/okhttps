package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.WebSocket;
import com.ejlchina.okhttps.WebSocket.Message;
import org.junit.Test;

public class WebSocketTests {

    @Test
    public void test() throws InterruptedException {

        HTTP http = HTTP.builder()
//                .baseUrl("ws://123.207.136.134:9010")
                .build();

        String url = "ws://121.40.165.18:8800";

        WebSocket socket = http.webSocket(url)
                .tag("AAA")
                .setOnOpen((WebSocket webSocket, HttpResult result) -> {
                    System.out.println("连接已打开：" + result);
//                    webSocket.send("Hello");
                })
                .setOnMessage((WebSocket webSocket, Message message) -> {
                    System.out.println("接收到消息：" + message.toString());
                })
                .setOnException((WebSocket webSocket, Throwable throwable) -> {
                    System.out.println("连接异常：" + throwable);
                })
                .setOnClosing((WebSocket webSocket, WebSocket.Close close) -> {
                    System.out.println("正在关闭：" + close);
                })
                .setOnClosed((WebSocket webSocket, WebSocket.Close close) -> {
                    System.out.println("已关闭：" + close);
                })
                .listen();

        Thread.sleep(2000);

//        socket.send("你好呀");
//        System.out.println("发送信息");
        Thread.sleep(2000);
        System.out.println("关闭");
        http.cancel("AAA");
//        socket.close(1000, "close");

        Thread.sleep(10000);
        System.out.println("。。。");
    }


}
