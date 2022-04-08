package com.ejlchina.okhttps;

/**
 * 进度（上传或下载）
 */
public interface Process {

	int DEFAULT_STEP_BYTES = 8192;
	
	/**
	 * 当请求体大小未知（流式上传）时，该方法始终返回 -1
	 * 当请求体大小为 0 时，该方法始终返回 1
	 * @return 完成比例
	 */
	double getRate();

	/**
	 * 当请求体大小未知（流式上传）时，该方法返回 -1
	 * @return 总字节数
	 */
	long getTotalBytes();
	
	/**
	 * @return 已完成字节数
	 */
	long getDoneBytes();
	
	/**
	 * 只有当 当请求体大小已知 的情况下，该方法才可能返回 true
	 * @return 任务是否完成
	 */
	boolean isDone();
	
}
