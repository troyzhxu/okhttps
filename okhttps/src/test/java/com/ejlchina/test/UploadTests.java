package com.ejlchina.test;

import cn.zhxu.okhttps.Process;
import okhttp3.mockwebserver.MockResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class UploadTests extends BaseTest {

    private byte[] randomFileContent(int length) {
        Random random = new Random();
        byte[] content = new byte[length];
        for (int i = 0; i < length; i++) {
            content[i] = (byte) (random.nextInt() % 256);
        }
        return content;
    }

    @Test
    public void testUpload() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        // 512 KB
        byte[] content = randomFileContent(5 * 1024);
        server.enqueue(new MockResponse().setResponseCode(200).setBody("OK"));
        long t0 = System.currentTimeMillis();
        http.async("/upload")
                .addBodyPara("path", "123456")
                .addFilePara("file", "txt", "222", content)
                .stepRate(0.1)
                .setOnProcess((Process process) -> println(t0, "上传：" + process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate()))
                .setOnResponse(res -> {
                    String result = res.getBody().toString();
                    println(result);
                    Assert.assertEquals(result, "OK");
                })
                .setOnComplete(state -> latch.countDown())
                .post();
        byte[] uploadData = server.takeRequest().getBody().buffer().readByteArray();
        println("数据长度：" + uploadData.length);
        latch.await();
    }

}
