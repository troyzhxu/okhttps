package com.ejlchina.okhttps;

/**
 * 进度（上传或下载）
 */
public interface Process {

	int DEFAULT_STEP_BYTES = 8192;
	
	/**
	 * @return 完成比例
	 */
	double getRate();

	/**
	 * @return 总
	 */
	long getTotalBytes();
	
	/**
	 * @return 已完成任务量
	 */
	long getDoneBytes();
	
	/**
	 * @return 任务是否完成
	 */
	boolean isDone();
	
}
