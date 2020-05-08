package com.ejlchina.okhttps;

/**
 * 任务监听接口
 * @author 15735
 * @since 2.3.0
 */
public interface TaskListener<T> {

	
	/**
	 * 全局任务监听
	 * @param task 所属的 HttpTask
	 * @param data 监听内容
	 * @return 是否继续执行 task 对应的回调函数
	 */
	boolean listen(HttpTask<?> task, T data);

	
}
