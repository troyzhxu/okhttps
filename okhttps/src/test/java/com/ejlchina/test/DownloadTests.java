package com.ejlchina.test;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.Process;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class DownloadTests extends BaseTest {


    /**
     * 分块下载测试
     * @param args
     */
    public static void main(String[] args) {
        long totalSize = HttpUtils.sync("/download/test.zip").get().getBody()
                .close()                             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
                .getContentLength();
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
                .request("OPTIONS");
        sleep(5000);
    }

    @Test
    public void testDownload() {
        HTTP http = HTTP.builder()
                .config((OkHttpClient.Builder builder) -> {
                    builder.readTimeout(300, TimeUnit.MILLISECONDS);
                })
                .build();

        String url = "https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe";
//		String url = "http://47.100.7.202/ejl-test.zip";

        long t0 = now();

//		Ctrl ctrl =
        http.sync(url)
//				.setRange(24771214)
                .bind(this)
                .get()
                .getBody()
                .setOnProcess((Process process) -> {
                    println(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .setStepRate(0.1)
                .toFolder("D:/WorkSpace/download/")
//				.toFile("D:\\WorkSpace\\download\\CocosDashboard-v1.0.1-win32-031816(9).exe")
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
