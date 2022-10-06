package cn.zhxu.okhttps;

import okhttp3.mockwebserver.MockResponse;
import org.junit.Assert;
import org.junit.Test;

public class CacheTests extends BaseTest {

    /**
     * 启用 cache 示例
     */
    @Test
    public void testCache() {
        String content = "test cache method";
        server.enqueue(new MockResponse().setBody(content));
        HttpResult.Body body = http.sync("/users").get().getBody()
                .cache();   // 启用 cache
        // 使用 cache 后，可以多次使用 toXXX() 方法
        Assert.assertEquals(content, body.toString());
        Assert.assertEquals(content, body.toString());
    }

}
