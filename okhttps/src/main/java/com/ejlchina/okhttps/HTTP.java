package com.ejlchina.okhttps;

import com.ejlchina.okhttps.okhttp.OkHttpBuilderImpl;
import okhttp3.WebSocket;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * HTTP 客户端接口
 *
 * @author 15735
 */
public interface HTTP {

    String GET = "GET";
    String HEAD = "HEAD";
    String POST = "POST";
    String PUT = "PUT";
    String PATCH = "PATCH";
    String DELETE = "DELETE";

    /**
     * 同步请求
     * @param url 请求地址
     * @return 同步HTTP任务
     */
    SHttpTask sync(String url);
    
    /**
     * 异步请求
     * @param url 请求地址
     * @return 异步HTTP任务
     */
    AHttpTask async(String url);

    /**
     * Websocket 连接
     * @param url 连接地址
     * @return WebSocket 任务
     */
    WHttpTask webSocket(String url);
    
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
     * OkHttp 的 HTTP 原生请求 （该请求不经过 预处理器）
     * @param request 请求
     * @return Call
     */
    Call request(Request request);

    /**
     * OkHttp 的 Websocket 原生请求（该请求不经过 预处理器）
     * @param request  请求
     * @param listener 监听器
     * @return Websocket
     */
    WebSocket webSocket(Request request, WebSocketListener listener);

    /**
     * 获取任务执行器
     * @return TaskExecutor
     */
    TaskExecutor executor();

    /**
     * 新的构建器
     * @return Builder
     */
    Builder newBuilder();
    
    /**
     * HTTP 构建器
     * 自 v3.5.0 起可通过系统环境变量 切换该方法返回的 构建器实现类
     * @return HTTP 构建器
     */
    static Builder builder() {
        String className = System.getProperty(Builder.class.getName());
        if (Platform.isNotBlank(className)) {
            try {
                Class<?> dClass = Class.forName(className);
                if (Builder.class.isAssignableFrom(dClass)) {
                    return (Builder) dClass.getDeclaredConstructor().newInstance();
                } else {
                    throw new OkHttpsException("The implementation class [" + className + "] you specified is not a subclass of " + Builder.class.getName());
                }
            } catch (ClassNotFoundException e) {
                throw new OkHttpsException("The implementation class [" + className + "] you specified can not be found", e);
            } catch (NoSuchMethodException e) {
                throw new OkHttpsException("There is none default constructor in [" + className + "] you specified", e);
            } catch (ReflectiveOperationException e) {
                throw new OkHttpsException("[" + className + "] can not be instanced", e);
            }
        }
        return new OkHttpBuilderImpl();     // 默认构建器
    }

    /**
     * Http 配置器
     *
     */
    interface OkConfig {

        /**
         * 使用 builder 配置 HttpClient
         * @param builder OkHttpClient 构建器
         */
        void config(OkHttpClient.Builder builder);

    }

    /**
     * HTTP 构建器
     * 自 v3.3.0 开始接口化，在以后版本中将逐步与 okhttp3 解耦
     */
    interface Builder {

        /**
         * 自 v3.2.0 后可以多次调用
         * 配置 OkHttpClient
         * @param config 配置器
         * @return Builder
         */
        Builder config(OkConfig config);

        /**
         * 设置 baseUrl
         * @param baseUrl 全局URL前缀
         * @return Builder
         */
        Builder baseUrl(String baseUrl);

        String baseUrl();

        /**
         * 配置媒体类型
         * @param mediaTypes 媒体类型
         * @return Builder
         */
        Builder mediaTypes(Map<String, String> mediaTypes);

        /**
         * 配置媒体类型
         * @param key 媒体类型KEY
         * @param value 媒体类型VALUE
         * @return Builder
         */
        Builder mediaTypes(String key, String value);

        Map<String, String> getMediaTypes();

        /**
         * 清空 ContentType
         * @return Builder
         * @since v3.5.0
         */
        HTTP.Builder clearContentTypes();

        /**
         * 配置支持的报文体类型
         * @param contentTypes 报文体类型列表
         * @return Builder
         */
        Builder contentTypes(List<String> contentTypes);

        /**
         * 配置支持的报文体类型
         * @param contentType 报文体类型
         * @return Builder
         */
        Builder contentTypes(String contentType);

        String[] contentTypes();

        /**
         * 设置回调执行器，例如实现切换线程功能，只对异步请求有效
         * @param executor 回调执行器
         * @return Builder
         */
        Builder callbackExecutor(Executor executor);

        Executor mainExecutor();

        /**
         * 配置 任务调度器，可用的调度由 {@link WHttpTask#heatbeat(int, int) } 指定的心跳任务
         * 若不配置，则生成一个 线程容量为 1 的 ScheduledThreadPoolExecutor 调度器
         * @since v2.3.0
         * @param scheduler 调度器
         * @return Builder
         */
        Builder taskScheduler(Scheduler scheduler);

        Scheduler taskScheduler();

        /**
         * 添加可并行处理请求任务的预处理器
         * @param preprocessor 预处理器
         * @return Builder
         */
        Builder addPreprocessor(Preprocessor preprocessor);

        /**
         * 添加串行预处理器
         * @param preprocessor 预处理器
         * @return Builder
         */
        Builder addSerialPreprocessor(Preprocessor preprocessor);

        /**
         * 清空预处理器（包括串行预处理器）
         * @since v2.5.0
         * @return Builder
         */
        Builder clearPreprocessors();

        Preprocessor[] preprocessors();

        /**
         * 最大预处理时间（倍数，相当普通请求的超时时间）
         * @param times 普通超时时间的倍数，默认为 10
         * @return Builder
         */
        Builder preprocTimeoutTimes(int times);

        int preprocTimeoutTimes();

        /**
         * 设置全局响应监听
         * @param listener 监听器
         * @return Builder
         */
        Builder responseListener(TaskListener<HttpResult> listener);

        TaskListener<HttpResult> responseListener();

        /**
         * 设置全局异常监听
         * @param listener 监听器
         * @return Builder
         */
        Builder exceptionListener(TaskListener<IOException> listener);

        TaskListener<IOException> exceptionListener();

        /**
         * 设置全局完成监听
         * @param listener 监听器
         * @return Builder
         */
        Builder completeListener(TaskListener<HttpResult.State> listener);

        TaskListener<HttpResult.State> completeListener();

        /**
         * 设置下载监听器
         * @param listener 监听器
         * @return Builder
         */
        Builder downloadListener(DownListener listener);

        DownListener downloadListener();

        /**
         * @since v2.0.0
         * 添加消息转换器
         * @param msgConvertor JSON 服务
         * @return Builder
         */
        Builder addMsgConvertor(MsgConvertor msgConvertor);

        /**
         * 清空消息转换器
         * @since v2.5.0
         * @return Builder
         */
        Builder clearMsgConvertors();

        MsgConvertor[] msgConvertors();

        /**
         * @since v2.0.0
         * 设置默认编码格式
         * @param charset 编码
         * @return Builder
         */
        Builder charset(Charset charset);

        Charset charset();

        /**
         * @since v2.0.0
         * 设置默认请求体类型
         * @param bodyType 请求体类型
         * @return Builder
         */
        Builder bodyType(String bodyType);

        String bodyType();

        /**
         * @since v3.4.2
         * 设置下载文件名解析器
         * @param resolver 解析器
         * @return Builder
         */
        Builder downloadHelper(DownloadHelper resolver);

        DownloadHelper downloadHelper();

        /**
         * 构建 HTTP 实例
         * @return HTTP
         */
        HTTP build();

    }

}
