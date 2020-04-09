package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.Process;
import org.junit.Test;

public class ExecutorTests extends BaseTest {


    @Test
    public void testExecutor() {
        HTTP http = HTTP.builder()
                .callbackExecutor((Runnable command) -> {
                    println("主线程执行");
                    command.run();
                }).build();

        http.async("http://47.100.7.202/ejl-test.zip")
                .addBodyParam("name", "Jack")
//				.nextOnIO()
                .setOnProcess((Process process) -> {
                    println("process： " + process.getRate());
                })
//				.nextOnIO()
                .setOnResponse((HttpResult result) -> {
                    println("status： " + result.close().getStatus());
                })
                .nextOnIO()
                .setOnComplete((HttpResult.State state) -> {
                    println("state： " + state);
                })
                .post();

        sleep(3000);
    }


}
