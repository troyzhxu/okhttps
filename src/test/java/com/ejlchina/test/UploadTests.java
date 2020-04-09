package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.Process;
import org.junit.Test;

public class UploadTests extends BaseTest {

    @Test
    public void testUpload() {
        String data = "0123456789abcdefghijklmnopqrstuvwsyz0123456789abcdefghijklmnopqrstuvwsyz0123456789abcdefghijklmnopqrstuvwsyz0123456789abcdefghijklmnopqrstuvwsyz0123456789abcdefghijklmnopqrstuvwsyz";

        HTTP http = HTTP.builder().build();

        long t0 = System.currentTimeMillis();

        String res = http.sync("http://localhost:8080/test/index")
                .addBodyParam("data", data)
                .addFileParam("file", "D:\\WorkSpace\\download\\CocosDashboard-v1.0.1-win32-031816.exe")
                .setStepRate(0.01)
                .setOnProcess((Process process) -> {
                    println(t0, "上传：" + process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .post()
                .getBody()
                .setStepBytes(5)
                .setOnProcess((Process process) -> {
                    println(t0, "下载：" + process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .toString();

        println("响应：" + res);
    }


}
