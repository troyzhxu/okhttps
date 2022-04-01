package com.ejlchina.stomp;

import com.ejlchina.okhttps.OkHttps;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws InterruptedException {

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("login", "admin"));
        headers.add(new Header("passcode", "eiotyunmq"));
        headers.add(new Header("host", "test"));

        Stomp stomp = Stomp.over(OkHttps.webSocket("ws://172.31.0.202:15674/ws").heatbeat(10, 10))
                .setOnConnected(s -> {
                    System.out.println("OnConnected");
                })
                .connect(headers);

        stomp.topic("/aaa", msg -> {
            System.out.println("收到消息：" + msg);
            System.out.println("内容：" + msg.getPayload());
        });

        Thread.sleep(1000000);

    }

}
