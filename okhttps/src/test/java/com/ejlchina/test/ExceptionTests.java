package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.internal.HttpException;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * 测试异常处理
 */
public class ExceptionTests extends BaseTest {

    HTTP http = HTTP.builder()
            .config((OkHttpClient.Builder builder) -> {
                builder.connectTimeout(2, TimeUnit.SECONDS);
                builder.writeTimeout(2, TimeUnit.SECONDS);
                builder.readTimeout(2, TimeUnit.SECONDS);
            })
            .baseUrl("http://localhost:8081")
            .build();

    @Test
    public void testSync0() {
        try {
            HttpResult result = http.sync("/users/1").get();
            System.out.println("请求结果：" + result);
        } catch (HttpException e) {
            Throwable cause = e.getCause(); // 得到异常原因
            if (cause instanceof ConnectException) {
                // 当没网络时，会抛出连接异常
            }
            if (cause instanceof SocketTimeoutException) {
                // 当接口长时间未响应，会抛出超时异常
            }
            if (cause instanceof UnknownHostException) {
                // 当把域名或IP写错，会抛出 UnknownHost 异常
            }
            // ...
        }
    }

    @Test
    public void testSync1() {
        HttpResult result = http.sync("/users/1")
                .nothrow()      // 告诉 OkHttps 发生异常时不要直接向外抛出
                .get();
        // 判断执行状态
        switch (result.getState()) {
            case RESPONSED:     // 请求已正常响应

                break;
            case CANCELED:      // 请求已被取消

                break;
            case NETWORK_ERROR: // 网络错误，说明用户没网了

                break;
            case TIMEOUT:       // 请求超时

                break;
            case EXCEPTION:     // 其它异常

                break;
        }
        // 还可以获得具体的异常信息
        Exception error = result.getError();
        System.out.println(error);

    }

    @Test
    public void testASync() {

        http.async("/users/1")
                .setOnResponse((HttpResult result) -> {
                    // 当发生异常时就不会走这里
                    println(result);
                })
                .setOnException((IOException e) -> {
                    // 这里处理请求异常
                    println(e);
                })
                .get();

        sleep(10000);
    }


}
