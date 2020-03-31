package com.ejlchina.test;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.ejlchina.okhttps.Download;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpCall;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.HttpUtils;
import com.ejlchina.okhttps.Process;
import com.ejlchina.okhttps.Download.Ctrl;
import com.ejlchina.okhttps.Download.Failure;
import com.ejlchina.okhttps.HttpResult.Body;
import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.Preprocessor.PreChain;
import com.ejlchina.okhttps.internal.HttpClient;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;


public class HttpTest {

	
	@Test
	public void testCache() {
		HTTP http = HTTP.builder().build();
		
		Body body = http.sync("http://xxx.cdyun.vip/comm/provinces")
				.get()
				.getBody()
				.cache()
				;
		
		
		System.out.println("result = " + body.toString());
		System.out.println("result = " + body.toJsonArray());
		System.out.println("result = " + body.toBytes());
		
		body.close();
		
		System.out.println("result = " + body.toString());
		System.out.println("result = " + body.toJsonArray());
		System.out.println("result = " + body.toBytes());
	}
	
	
	@Test
	public void testGlobalCallback() {
		HTTP http = HTTP.builder()
				.responseListener((HttpTask<?> task, HttpResult result) -> {
					System.out.println("全局 onResponse: " + result.getStatus());
					return true;
				})
				.completeListener((HttpTask<?> task, State state) -> {
					System.out.println("全局 onComplete: " + state);
					return true;
				})
				.exceptionListener((HttpTask<?> task, IOException error) -> {
					System.out.println("全局 onException: " + error.getMessage());
					return false;
				})
				.build();
		
		http.async("http://www.baidu.com")
			.setOnResponse((HttpResult result) -> {
				System.out.println("单例 onResponse: " + result.getStatus());
			})
			.setOnComplete((State state) -> {
				System.out.println("单例 onComplete: " + state);
			})
			.setOnException((IOException error) -> {
				System.out.println("单例 onException: " + error.getMessage());
			})
			.get();
		
		sleep(2000);
	}
	
	@Test
	public void testDownloadListener() {
		HTTP http = HTTP.builder()
				.downloadListener((HttpTask<?> task, Download download) -> {
					System.out.println("URL = " + task.getUrl());
					System.out.println("TAG = " + task.getTag());
					Ctrl ctrl = download.getCtrl();
					
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

		long t0 = System.currentTimeMillis();
		

		http.sync(url)
				.setTag("ASD")
				.get()
				.getBody()
				.setOnProcess((Process process) -> {
					print(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate(), false);
				})
				.setStepRate(0.01)
				.toFolder("D:/download/")
				.nextOnIO()
				.setOnSuccess((File file) -> {
					print(t0, "下载成功：" + file.getAbsolutePath(), true);
				})
				.setOnFailure((Failure failure) -> {
					print(t0, "下载失败：" + failure.getDoneBytes() + ", path = " + failure.getFile().getAbsolutePath(), true);
				})
				.start();
		
		sleep(20000);
	}
	
	@Test
	public void testExecutor() {
		HTTP http = HTTP.builder()
				.callbackExecutor((Runnable command) -> {
					System.out.println("主线程执行");
					command.run();
				}).build();
		
		http.async("http://47.100.7.202/ejl-test.zip")
				.addBodyParam("name", "Jack")
//				.nextOnIO()
				.setOnProcess((Process process) -> {
					System.out.println("process： " + process.getRate());
				})
//				.nextOnIO()
				.setOnResponse((HttpResult result) -> {
					System.out.println("status： " + result.close().getStatus());
				})
				.nextOnIO()
				.setOnComplete((State state) -> {
					System.out.println("state： " + state);
				})
				.post();
		
		sleep(3000);
	}
	
	
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
	                    System.out.println("下载完成");
	                }
	            })
	            .start();
	}
	
	
	@Test
	public void testD() {
		System.out.println(Long.parseLong("12"));
	}
	
	
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
				print(t0, "上传：" + process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate(), false);
			})
			.post()
			.getBody()
			.setStepBytes(5)
			.setOnProcess((Process process) -> {
				print(t0, "下载：" + process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate(), false);
			})
			.toString();
		
		System.out.println("响应：" + res);
	}
	
	@Test
	public void testDownload() {
		HTTP http = HTTP.builder()
				.config((Builder builder) -> {
					builder.readTimeout(300, TimeUnit.MILLISECONDS);
				})
				.build();
		
		String url = "https://download.cocos.com/CocosDashboard/v1.0.1/CocosDashboard-v1.0.1-win32-031816.exe";
//		String url = "http://47.100.7.202/ejl-test.zip";

		long t0 = System.currentTimeMillis();
		
//		Ctrl ctrl = 
		http.sync(url)
//				.setRange(24771214)
				.bind(this)
				.get()
				.getBody()
				.setOnProcess((Process process) -> {
					print(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate(), false);
				})
				.setStepRate(0.1)
				.toFolder("D:/WorkSpace/download/")
//				.toFile("D:\\WorkSpace\\download\\CocosDashboard-v1.0.1-win32-031816(9).exe")
//				.setAppended() // 启用 断点续传
				.nextOnIO()
				.setOnSuccess((File file) -> {
					print(t0, "下载成功：" + file.getAbsolutePath(), true);
				})
				.setOnFailure((Failure failure) -> {
					print(t0, "下载失败：" + failure.getDoneBytes() + ", path = " + failure.getFile().getAbsolutePath(), true);
				})
				.start();
		
		sleep(10000);
		
//		ctrl.status();
//		ctrl.pause();
//		System.out.println("暂停");
//		sleep(5000);
//		
//		ctrl.resume();
//		System.out.println("继续");
//		sleep(5000);
//		
//		ctrl.cancel();
//		System.out.println("取消");
		

//		

//		
//		ctrl.pause();
//		System.out.println("暂停");
//		sleep(5000);
//		
//		ctrl.resume();
//		System.out.println("继续");
		sleep(5000);
		
	}
	
	void print(long t0, String str, boolean ln) {
		long now = System.currentTimeMillis() - t0;
		System.out.println((now) + "\t" + str);
		if (ln) {
			System.out.println();
		}
	}
	
	long now(long t0) {
		return System.currentTimeMillis() - t0;
	}

	
	@Test
	public void testToList() {
		long t0 = System.currentTimeMillis();
		
		HTTP http = HTTP.builder()
				.baseUrl("http://xxx.cdyun.vip/ejlchina")
				.build();
		
		HttpResult result = http.sync("/comm/provinces")
				.setRange(0)
				.get();
		
		print(t0, "status: " + result.getStatus(), true);
		print(t0, "headers: " + result.getHeaders(), true);
		
		Body body = result.getBody();

		print(t0, "total: " + body.getContentLength(), true);
		
		List<User> list = body.setStepRate(0.1)
				.setOnProcess((Process process) -> {
					print(t0, process.getDoneBytes() + "/" + process.getTotalBytes() + "\t" + process.getRate(), false);
				})
				.toList(User.class);
		
		print(t0, list.toString(), false);
	}
	
	
	@Test
	public void testPreprocessor() {
		
		HTTP http = HTTP.builder()
			.baseUrl("http://localhost:8080")
			.addPreprocessor((PreChain chain) -> {
				System.out.println("并行预处理-开始");
//				new Thread(() -> {
					sleep(2000);
					System.out.println("并行预处理-结束");
					chain.proceed();
//				}).start();
			})
			.addSerialPreprocessor((PreChain chain) -> {
				System.out.println("串行预处理-开始");
				new Thread(() -> {
					sleep(3000);
					System.out.println("串行预处理-结束");
					chain.proceed();
				}).start();
			})
			.build();
		
		new Thread(() -> {
			System.out.println(http.sync("/user/show/1").get());
		}).start();
		
		new Thread(() -> {
			System.out.println(http.sync("/user/show/2").get());
		}).start();
		
//		new Thread(() -> {
//			http.async("/user/show/1")
//				.setOnResponse((HttpResult result) -> {
//					System.out.println(result);
//				})
//				.get();
//		}).start();
//		
//		new Thread(() -> {
//			http.async("/user/show/2")
//				.setOnResponse((HttpResult result) -> {
//					System.out.println(result);
//				})
//				.get();
//		}).start();
		
		sleep(10000);
	}
	


	@Test
	public void testCancel() {
		
		HTTP http = HTTP.builder()
			.baseUrl("http://localhost:8080")
			.build();
		
		http.async("/user/show/1")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.setOnException((IOException e) -> {
					System.out.println("异常捕获：" + e.getMessage());
				})
				.setOnComplete((State state) -> {
					System.out.println(state);
				})
				.setTag("A")
				.get();

		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/2")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.setTag("A.B")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/3")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.setTag("B.C")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		http.async("/user/show/4")
				.setOnResponse((HttpResult result) -> {
					System.out.println(result);
				})
				.setTag("C")
				.get();
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		
		System.out.println("标签取消：" + http.cancel("B"));
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		sleep(5000);

		System.out.println(((HttpClient) http).getTagCallCount());
		
		sleep(5000);
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
		sleep(5000);
		
		System.out.println(((HttpClient) http).getTagCallCount());
		
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
//		
//		System.out.println("取消结果 = " + call.cancel());
//		
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
//		
//		sleep(100);
//		System.out.println("++++++++");
//		
//		System.out.println("isDone = " + call.isDone());
//		System.out.println("isCanceled = " + call.isCanceled());
	}
	

	
	
	private HTTP buildHttp() {
		
		return HTTP.builder()
				.config((Builder builder) -> {
					
					// 配置连接池 最小10个连接（不配置默认为 5）
					builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
					
					// 配置连接超时时间
					builder.connectTimeout(20, TimeUnit.SECONDS);
					
					builder.addInterceptor((Chain chain) -> {
						
						Request request = chain.request();

						return chain.proceed(request);
					});
				})
				.baseUrl("http://localhost:8080")
				.callbackExecutor((Runnable run) -> {
					runOnUiThread(run);
				})
				.addPreprocessor((PreChain chain) -> {
					new Thread(() -> {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						chain.getTask().addHeader("Token", "yyyyy");
				
						chain.proceed();
						
					}).start();
				})
				.addPreprocessor((PreChain chain) -> {
					new Thread(() -> {

						chain.getTask().addUrlParam("actor", "Alice");
				
						chain.proceed();
				
					}).start();
				})
				.build();
		
	}
	

	@Test
	public void syncHttpExample() {
		
		HTTP http = buildHttp();
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
//		System.out.println("status = " + status);
//		System.out.println("headers = " + headers);
//		System.out.println("user = " + user);
		
		System.out.println("status = " + result.getStatus());
		
	}


	@Test
	public void syncJsonExample() {
		HTTP http = buildHttp();
		// 同步请求
		HttpResult result = http.sync("/user/save")
				.addJsonParam("name", "Tom")
				.addJsonParam("age", 23)
				.post();
		
		System.out.println("result = " + result);
		
		result = http.sync("/user/show/1").get();
		
		System.out.println("result = " + result);
		
		System.out.println("isSuccessful = " + result.isSuccessful());
	}

	@Test
	public void asyncHttpExample() throws InterruptedException {
		HTTP http = buildHttp();
		// 异步请求
		// 最终路径 http://api.demo.com/users/2
		HttpCall call = http.async("/user/show/{id}")
				// 设置路径参数
				.addPathParam("id", 2)
				// 设置回调函数
				.setOnResponse((HttpResult result) -> {
					System.out.println("000");
//					User user = result.getBody().toBean(User.class);
//					System.out.println("user = " + user);
				})
				.setOnException((IOException e) -> {
					e.printStackTrace();
				})
				// 发起  GET 请求
				.get();
		
		Thread.sleep(150);
		
		System.out.println("是否完成: " + call.isDone());
		System.out.println("是否取消: " + call.isCanceled());
		
		call.cancel();  // 取消请求
		
		System.out.println("是否取消: " + call.isCanceled());
		System.out.println("执行结果: " + call.getResult());
		System.out.println("是否完成: " + call.isDone());
//		
		Thread.sleep(100);
//		
//		System.out.println("是否完成: " + call.isDone());
//		System.out.println("是否取消: " + call.isCanceled());
//		System.out.println("执行状态: " + call.getResult());
		
	}

	
	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	static void runOnUiThread(Runnable run) {
		run.run();
	}
	
}
