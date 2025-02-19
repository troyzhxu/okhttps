package cn.zhxu.okhttps;

/**
 * 预处理器，支持异步
 * 在 HTTP 请求任务正式开始之前执行
 * @author Troy.Zhou
 */
public interface Preprocessor {

	/**
	 * 在 HTTP 请求开始之前执行
	 * @param chain 预处理器链
	 */
	void doProcess(PreChain chain);
	
	
	interface PreChain {
		
		/**
		 * @return 当前的请求任务
		 */
		HttpTask<?> getTask();

		/**
		 * @return 当前使用的 HTTP 实例
		 */
		HTTP getHttp();
		
		/**
		 * 继续 HTTP 请求任务
		 */
		void proceed();
		
	}
	
}
