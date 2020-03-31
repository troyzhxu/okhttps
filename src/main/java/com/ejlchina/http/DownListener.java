package com.ejlchina.http;

/**
 * 下载监听接口
 * @author 15735
 * @since 2.3.0
 */
public interface DownListener {

	
	/**
	 * 全局下载监听
	 * @param task 所属的 HttpTask
	 * @param download 下载事件
	 */
	void listen(HttpTask<?> task, Download download);

	
}
