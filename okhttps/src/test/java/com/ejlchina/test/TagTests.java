package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.internal.AbstractHttpClient;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TagTests extends BaseTest {

    @Test
    public void testTag() {
        AbstractHttpClient http = (AbstractHttpClient) HTTP.builder()
                .config((OkHttpClient.Builder builder) -> {
                    builder.connectTimeout(1, TimeUnit.SECONDS);
                    builder.writeTimeout(1, TimeUnit.SECONDS);
                    builder.readTimeout(1, TimeUnit.SECONDS);

                    builder.pingInterval(60, TimeUnit.SECONDS);
                })
                .build();

        http.async("http://www.baidu.com")
                .tag("AA")
                .setOnResponse((HttpResult result) -> {
                    throw new RuntimeException("我是一个异常");
                })
                .get();

        println("taskCount = " + http.getTagTaskCount());
        sleep(1000);
        println("taskCount = " + http.getTagTaskCount());
        sleep(1000);
        println("taskCount = " + http.getTagTaskCount());
        sleep(5000);
        println("taskCount = " + http.getTagTaskCount());

    }


}
