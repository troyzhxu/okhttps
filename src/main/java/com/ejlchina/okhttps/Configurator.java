package com.ejlchina.okhttps;

import okhttp3.OkHttpClient.Builder;

/**
 * Http 配置器
 *
 */
public interface Configurator {
	
	/**
	 * 使用 builder 配置 HttpClient
	 * @param builder OkHttpClient 构建器
	 */
	void config(Builder builder);
	
}
