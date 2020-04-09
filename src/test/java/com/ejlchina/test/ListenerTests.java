package com.ejlchina.test;

import com.ejlchina.okhttps.Download;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.Process;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ListenerTests extends BaseTest {

    @Test
    public void testGlobalCallback() {
        HTTP http = HTTP.builder()
                .responseListener((HttpTask<?> task, HttpResult result) -> {
                    println("全局 onResponse: " + result.getStatus());
                    return true;
                })
                .completeListener((HttpTask<?> task, HttpResult.State state) -> {
                    println("全局 onComplete: " + state);
                    return true;
                })
                .exceptionListener((HttpTask<?> task, IOException error) -> {
                    println("全局 onException: " + error.getMessage());
                    return false;
                })
                .build();

        http.async("http://www.baidu.com")
                .setOnResponse((HttpResult result) -> {
                    println("单例 onResponse: " + result.getStatus());
                })
                .setOnComplete((HttpResult.State state) -> {
                    println("单例 onComplete: " + state);
                })
                .setOnException((IOException error) -> {
                    println("单例 onException: " + error.getMessage());
                })
                .get();

        sleep(2000);
    }

    @Test
    public void testDownloadListener() {
        HTTP http = HTTP.builder()
                .downloadListener((HttpTask<?> task, Download download) -> {
                    println("URL = " + task.getUrl());
                    println("TAG = " + task.getTag());
                    Download.Ctrl ctrl = download.getCtrl();

                    new Thread(() -> {
                        sleep(3000);
                        ctrl.pause();
                        sleep(3000);
                        ctrl.resume();
                        sleep(3000);
                        ctrl.cancel();
                    }).start();
                })
                .build();

        String url = "https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe";

        long t0 = now();


        http.sync(url)
                .setTag("ASD")
                .get()
                .getBody()
                .setOnProcess((Process process) -> {
                    println(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate());
                })
                .setStepRate(0.01)
                .toFolder("D:/download/")
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

        sleep(20000);
    }

}
