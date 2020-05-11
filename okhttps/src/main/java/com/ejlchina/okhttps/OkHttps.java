package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.*;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * OkHttps 工具类
 * 支持 SPI 方式配置
 */
public final class OkHttps {

    private OkHttps() {}


    private static HTTP http;

    static synchronized HTTP getHttp() {
        if (http != null) {
            return http;
        }
        HTTP.Builder builder = HTTP.builder();
        ConvertProvider.inject(builder);
        Config.config(builder);
        http = builder.build();
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
     * Websocket 连接
     * @param url 连接地址
     * @return WebSocket 任务
     */
    public static WebSocketTask webSocket(String url) {
        return getHttp().webSocket(url);
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
     * @since 1.0.3
     * 取消所有HTTP任务，包括同步和异步
     */
    public void cancelAll() {
        getHttp().cancelAll();
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
     * @return WebSocket
     */
    public static WebSocket webSocket(Request request, WebSocketListener listener) {
        return getHttp().webSocket(request, listener);
    }

    /**
     * 获取任务执行器
     * @return TaskExecutor
     */
    public static TaskExecutor getExecutor() {
        return getHttp().executor();
    }

}
