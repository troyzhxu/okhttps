package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import org.junit.Test;

public class CacheTests extends BaseTest {

    @Test
    public void testCache() {
        HTTP http = HTTP.builder().build();

        HttpResult.Body body = http.sync("http://xxx.cdyun.vip/comm/provinces")
                .get()
                .getBody()
                .cache();

        println("result = " + body.toString());
        println("result = " + body.toArray());

        body.close();

        println("result = " + body.toString());
        println("result = " + body.toArray());
    }

}
