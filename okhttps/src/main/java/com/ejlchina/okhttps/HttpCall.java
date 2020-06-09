package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.AsyncHttpTask;

public interface HttpCall extends Cancelable {

	/**
	 * @return 是否被取消
	 */
	boolean isCanceled();

	/**
	 * @return 请求是否执行完成，包含取消和失败
	 */
	boolean isDone();
	
	/**
	 * Waits if necessary for the computation to complete, and then
     * retrieves its result
	 * @return 请求执行结果，若请求未执行完，则阻塞当前线程直到请求执行完成
	 */
	HttpResult getResult();

	/**
	 * @return 当前的异步请求任务
	 */
	AsyncHttpTask getTask();

}
