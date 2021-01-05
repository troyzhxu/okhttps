package com.ejlchina.okhttps;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ParallelTest {


    @Test
    public void test() throws InterruptedException {

        int size = 10 * 1024 * 1024;   // 每块最多下载 30 M

        CountDownLatch latch = new CountDownLatch(1);

        long t0 = System.currentTimeMillis();

        Parallel.download(Parallel.OK_HTTPS, "https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe")
                .partBytes(size)
                .toFile("D:/CocosDashboard.exe")
                .setOnSuccess(f -> {
                    long t1 = System.currentTimeMillis();
                    System.out.println("下载完成：" + f.getPath());
                    System.out.println("共耗时：" + (t1 - t0) + " 毫秒");
                    latch.countDown();
                })
                .start();

        latch.await();
    }


}
