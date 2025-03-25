package cn.zhxu.okhttps;

import org.junit.Test;

public class PreprocessorTests extends BaseTest {


    @Test
    public void testPreprocessor() {

        HTTP http = HTTP.builder()
                .baseUrl("http://localhost:8080")
                .addPreprocessor((Preprocessor.PreChain chain) -> {
                    println("并行预处理-开始");
//				new Thread(() -> {
                    sleep(2000);
                    println("并行预处理-结束");
                    chain.proceed();
//				}).start();
                })
                .addSerialPreprocessor((Preprocessor.PreChain chain) -> {
                    println("串行预处理-开始");
                    new Thread(() -> {
                        sleep(3000);
                        println("串行预处理-结束");
                        chain.proceed();
                    }).start();
                })
                .build();

        new Thread(() -> {
            println(http.sync("/user/show/1").get());
        }).start();

        new Thread(() -> {
            println(http.sync("/user/show/2").get());
        }).start();

//		new Thread(() -> {
//			http.async("/user/show/1")
//				.setOnResponse((HttpResult result) -> {
//					println(result);
//				})
//				.get();
//		}).start();
//
//		new Thread(() -> {
//			http.async("/user/show/2")
//				.setOnResponse((HttpResult result) -> {
//					println(result);
//				})
//				.get();
//		}).start();

        sleep(10000);
    }

}
