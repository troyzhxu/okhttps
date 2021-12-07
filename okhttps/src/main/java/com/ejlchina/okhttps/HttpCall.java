package com.ejlchina.okhttps;

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
	 * 获取所属的请求任务
	 * @since 2.2.0
	 * @return 当前的异步请求任务
	 */
	AHttpTask getTask();

}
