package com.ejlchina.test;

import com.ejlchina.okhttps.Process;
import com.ejlchina.okhttps.*;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;

public class DownloadTests extends BaseTest {


    /**
     * 分块下载测试
     * @param args
     */
    public static void main(String[] args) {
        long totalSize = HttpUtils.sync("/download/test.zip").get().getBody()
                .close()                             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
                .getLength();
        download(totalSize, 0);                      // 从第 0 块开始下载
        sleep(50000);                                // 等待下载完成
    }

    static void download(long totalSize, int index) {
        long size = 3 * 1024 * 1024;                 // 每块下载 3M
        long start = index * size;
        long end = Math.min(start + size, totalSize);
        HttpUtils.sync("/download/test.zip")
                .setRange(start, end)                // 设置本次下载的范围
                .get().getBody()
                .toFile("D:/download/test.zip")      // 下载到同一个文件里
                .setAppended()                       // 开启文件追加模式
                .setOnSuccess((File file) -> {
                    if (end < totalSize) {           // 若未下载完，则继续下载下一块
                        download(totalSize, index + 1);
                    } else {
                        println("下载完成");
                    }
                })
                .start();
    }

    @Test
    public void testDownload1() {
        HTTP http = HTTP.builder().build();
        http.async("https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe")
                .setOnResponse((HttpResult result) -> {
                    System.out.println(result.toString());
                    System.out.println("ContentLength = " + result.getContentLength());
                    System.out.println(result.getBody().getLength());
                    System.out.println("type = " + result.getBody().getType());
                    System.out.println("body = " + result.getBody().toString());
                })
                .head();
        sleep(5000);
    }

    /**
     * 多线程并行下载到同一个文件
     * 这种下载方法不一定高效，因为多线程向同一个文件内写入数据存在竞争，下载时间不一定会变短
     * 例如 文件总大小：82.3 MB (86,336,432 字节)：
     * 1 个线程并行下载: 7727 毫秒
     * 2 个线程并行下载: 7842 毫秒
     * 3 个线程并行下载: 8075 毫秒
     * 5 个线程并行下载: 8527 毫秒
     * 9 个线程并行下载: 8024 毫秒
     */
    @Test
    public void parallel() throws InterruptedException {
        long t0 = System.currentTimeMillis();

        String url = "https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe";

        long totalLength = OkHttps.sync(url).head().getContentLength();
        long size = 30 * 1024 * 1024;   // 每块最多下载 10 M

        int count = new BigDecimal(totalLength).divide(new BigDecimal(size), RoundingMode.UP).intValue();

        println("共需下载 " + count + "块");

        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            int index = i;
            long start = index * size;
            new Thread(() -> {
                OkHttps.sync(url)
                        .setRange(start, start + size)
                        .get()
                        .getBody()
                        .stepRate(0.1)
                        .setOnProcess(p -> println("下载进度 [" + index + "]: " + p))
                        .toFile("D:/CocosDashboard.exe")
                        .setOnSuccess((f) -> latch.countDown())
                        .start();
                println("已开始下载第 " + index + "块");
            }).start();
        }
        latch.await();

        long t1 = System.currentTimeMillis();

        println("共耗时：" + (t1 - t0) + " 毫秒");
    }


    @Test
    public void testRandomAccessFile() throws IOException {
        String inputFile = "G:/1.jpg";
        String outputFile = "G:/2.jpg";

        FileInputStream input = new FileInputStream(inputFile);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len;
        while ((len = input.read(buff)) != -1) {
            output.write(buff, 0, len);
        }
        input.close();
        byte[] data = output.toByteArray();
        output.close();
        System.out.println("data length = " + data.length);

        File outFile = new File(outputFile);
        if (!outFile.exists()) {
            outFile.createNewFile();
        }
        int size = data.length / 3;

        for (int i = 2; i >= 0; i--) {
            final int index = i;
            new Thread(() -> {
                try {
                    int start = index * size;
                    RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
                    raf.seek(start);
                    if (index < 2) {
                        raf.write(data, start, size);
                    } else {
                        raf.write(data, start, data.length - start);
                    }
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("第 " + index + " 块写入完成！");
            }).start();
        }

        sleep(5000);

    }



    @Test
    public void testDownload() {
        HTTP http = HTTP.builder()
                .config((OkHttpClient.Builder builder) -> {
//                    builder.readTimeout(300, TimeUnit.MILLISECONDS);
                })
                .build();

        String url = "https://gitee.com/ejlchina-zhxu/okhttps/blob/master/README.md";
//		String url = "http://47.100.7.202/ejl-test.zip";

        long t0 = now();

//		Ctrl ctrl =
        HttpResult result = http.sync(url)
//				.setRange(24771214)
                .bind(this)
                .get();

        println(result.getHeaders());
        println();

        result
                .getBody()
                .setOnProcess((Process process) -> {
                    println(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .stepRate(0.1)
                .toFolder("D:/WorkSpace/download/")
//				.toFile("D:/WorkSpace/test/README.md")
//				.setAppended() // 启用 断点续传
                .nextOnIO()
                .setOnSuccess((File file) -> {
                    println(t0, "下载成功：" + file.getAbsolutePath());
                    println();
                })
                .setOnFailure((Download.Failure failure) -> {
                    println(t0, "下载失败：" + failure.getDoneBytes() + ", path = " + failure.getFile().getAbsolutePath());
                    println();
                })
                .start();

        sleep(10000);

//		ctrl.status();
//		ctrl.pause();
//		println("暂停");
//		sleep(5000);
//
//		ctrl.resume();
//		println("继续");
//		sleep(5000);
//
//		ctrl.cancel();
//		println("取消");


//

//
//		ctrl.pause();
//		println("暂停");
//		sleep(5000);
//
//		ctrl.resume();
//		println("继续");
        sleep(5000);

    }


    @Test
    public void syncHttpExample() {

        HTTP http = HTTP.builder().build();
        // 同步请求
        HttpResult result = http.sync("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1584365400942&di=d9e7890f13b7bc4b76080fdd490ed5d5&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D1484500186%2C1503043093%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1280%26h%3D853")
                .get();

        result.getBody().toFile("E:/3.jpg");
//		// 得到状态码
//		int status = result.getStatus();
//
//		// 得到返回头
//		Headers headers = result.getHeaders();
//
//		User user = result.getBody().toBean(User.class);
//		// 得到目标数据
//
//
//		println("status = " + status);
//		println("headers = " + headers);
//		println("user = " + user);

        println("status = " + result.getStatus());

    }

}
