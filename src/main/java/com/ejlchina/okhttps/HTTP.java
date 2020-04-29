package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.AsyncHttpTask;
import com.ejlchina.okhttps.internal.SyncHttpTask;
import com.ejlchina.okhttps.internal.TaskExecutor;
import com.ejlchina.okhttps.internal.WebSocketTask;
import com.ejlchina.okhttps.internal.HttpClient.Builder;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * HTTP 客户端接口
 *
 * @author 15735
 */
public interface HTTP {

    /**
     * 异步请求
     * @param url 请求地址
     * @return 异步HTTP任务
     */
    AsyncHttpTask async(String url);

    /**
     * 异步请求
     * @return 异步HTTP任务
     */
    AsyncHttpTask async();

    /**
     * 同步请求
     * @param url 请求地址
     * @return 同步HTTP任务
     */
    SyncHttpTask sync(String url);

    /**
     * 同步请求
     * @return 同步HTTP任务
     */
    SyncHttpTask sync();

    /**
     * Websocket 连接
     * @param url 连接地址
     * @return WebSocket 任务
     */
    WebSocketTask webSocket(String url);
    
    /**
     * 根据标签取消HTTP任务，只要任务的标签包含指定的Tag就会被取消
     * 从 v1.0.2 开始支持取消同步请求
     * @param tag 标签
     * @return 被取消的任务数量
     */
    int cancel(String tag);

    /**
     * @since 1.0.2
     * 取消所有HTTP任务，包括同步和异步
     */
    void cancelAll();

    /**
     * OkHttp 原生请求 （该请求不经过 预处理器）
     * @param request 请求
     * @return Call
     */
    Call request(Request request);

    /**
     * Websocket（该请求不经过 预处理器）
     *
     * @param request  请求
     * @param listener 监听器
     * @return Websocket
     */
    WebSocket webSocket(Request request, WebSocketListener listener);

    /**
     * 获取任务执行器
     * @return TaskExecutor
     */
    TaskExecutor getExecutor();

    /**
     * HTTP 构建器
     * @return HTTP 构建器
     */
    static Builder builder() {
        return new Builder();
    }

}
