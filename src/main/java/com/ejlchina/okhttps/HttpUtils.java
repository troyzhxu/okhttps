package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.AsyncHttpTask;
import com.ejlchina.okhttps.internal.SyncHttpTask;
import com.ejlchina.okhttps.internal.TaskExecutor;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Http 工具类，封装 OkHttp

 * 特性： 
 *   同步请求
 *   异步请求
 *   Restfull路径
 *   文件上传
 *   JSON自动解析
 *   TCP连接池
 *   Http2
 *   
 * @author Troy.Zhou
 */
public class HttpUtils {


    private static HTTP http;


    /**
     * 配置HttpUtils持有的HTTP实例（不调用此方法前默认使用一个没有没有经过任何配置的HTTP懒实例）
     * @param http HTTP实例
     */
    public static void of(HTTP http) {
        if (http != null) {
            HttpUtils.http = http;
        }
    }


    static synchronized HTTP getHttp() {
        if (http != null) {
            return http;
        }
        http = HTTP.builder().build();
        return http;
    }

    /**
     * 异步请求
     * @param url 请求地址
     * @return 异步 HttpTask
     */
    public static AsyncHttpTask async(String url) {
        return getHttp().async(url);
    }

    /**
     * 同步请求
     * @param url 请求地址
     * @return 同步 HttpTask
     */
    public static SyncHttpTask sync(String url) {
        return getHttp().sync(url);
    }

    /**
     * 根据标签取消HTTP任务，只要任务的标签包含指定的Tag就会被取消
     * @param tag 标签
     * @return 被取消的任务数量
     */
    public static int cancel(String tag) {
        return getHttp().cancel(tag);
    }

    /**
     * OkHttp 原生请求 （该请求不经过 预处理器）
     * @param request 请求
     * @return Call
     */
    public static Call request(Request request) {
    	return getHttp().request(request);
    }
    
    /**
     * Websocket（该请求不经过 预处理器）
     * @param request 请求
     * @param listener 监听器
     * @return
     */
    public static WebSocket webSocket(Request request, WebSocketListener listener) {
    	return getHttp().webSocket(request, listener);
    }
    
    /**
     * 获取任务执行器
     * @return TaskExecutor
     */
    public static TaskExecutor getExecutor() {
    	return getHttp().getExecutor();
    }
    
}
