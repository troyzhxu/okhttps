package com.ejlchina.okhttps;

/**
 * 预处理器，支持异步
 * 在HTTP请求任务正式开始之前执行
 * @author Troy.Zhou
 */
public interface Preprocessor {

	/**
	 * 在HTTP请求开始之前执行
	 * @param chain 预处理器链
	 */
	void doProcess(PreChain chain);
	
	
	interface PreChain {
		
		/**
		 * @return 当前的请求任务
		 */
		HttpTask<?> getTask();
		
		/**
		 * @return HTTP
		 */
		HTTP getHttp();
		
		/**
		 * 继续HTTP请求任务
		 */
		void proceed();
		
	}
	
}
