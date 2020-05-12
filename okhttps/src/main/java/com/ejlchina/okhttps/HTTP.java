package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.*;

import okhttp3.*;
import okhttp3.WebSocket;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * HTTP 客户端接口
 *
 * @author 15735
 */
public interface HTTP {

    /**
     * 同步请求
     * @param url 请求地址
     * @return 同步HTTP任务
     */
    SyncHttpTask sync(String url);
    
    /**
     * 异步请求
     * @param url 请求地址
     * @return 异步HTTP任务
     */
    AsyncHttpTask async(String url);

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
     * OkHttp 的 HTTP 原生请求 （该请求不经过 预处理器）
     * @param request 请求
     * @return Call
     */
    Call request(Request request);

    /**
     * OkHttp 的 Websocket 原生请求（该请求不经过 预处理器）
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
    TaskExecutor executor();

    /**
     * 新的构建器
     * @return Builder
     */
    Builder newBuilder();
    
    /**
     * HTTP 构建器
     * @return HTTP 构建器
     */
    static Builder builder() {
        return new Builder();
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


    class Builder {

        private OkHttpClient okClient;

        private String baseUrl;

        private Map<String, String> mediaTypes;

        private OkConfig config;

        private Executor mainExecutor;

        private List<Preprocessor> preprocessors;

        private DownListener downloadListener;

        private TaskListener<HttpResult> responseListener;

        private TaskListener<IOException> exceptionListener;

        private TaskListener<HttpResult.State> completeListener;

        private List<MsgConvertor> msgConvertors;

        private int preprocTimeoutTimes = 10;

        private Charset charset = StandardCharsets.UTF_8;

        private String bodyType = "json";

        public Builder() {
            mediaTypes = new HashMap<>();
            mediaTypes.put("*", "application/octet-stream");
            mediaTypes.put("png", "image/png");
            mediaTypes.put("jpg", "image/jpeg");
            mediaTypes.put("jpeg", "image/jpeg");
            mediaTypes.put("wav", "audio/wav");
            mediaTypes.put("mp3", "audio/mp3");
            mediaTypes.put("mp4", "video/mpeg4");
            mediaTypes.put("txt", "text/plain");
            mediaTypes.put("xls", "application/x-xls");
            mediaTypes.put("xml", "text/xml");
            mediaTypes.put("apk", "application/vnd.android.package-archive");
            mediaTypes.put("doc", "application/msword");
            mediaTypes.put("pdf", "application/pdf");
            mediaTypes.put("html", "text/html");
            preprocessors = new ArrayList<>();
            msgConvertors = new ArrayList<>();
        }

        public Builder(HttpClient hc) {
            this.okClient = hc.okClient();
            this.baseUrl = hc.baseUrl();
            this.mediaTypes = hc.mediaTypes();
            this.preprocessors = new ArrayList<>();
            Collections.addAll(this.preprocessors, hc.preprocessors());
            TaskExecutor executor = hc.executor();
            this.downloadListener = executor.getDownloadListener();
            this.responseListener = executor.getResponseListener();
            this.exceptionListener = executor.getExceptionListener();
            this.completeListener = executor.getCompleteListener();
            this.msgConvertors = new ArrayList<>();
            Collections.addAll(this.msgConvertors, executor.getMsgConvertors());
            this.preprocTimeoutTimes = hc.preprocTimeoutTimes();
            this.charset = hc.charset();
            this.bodyType = hc.bodyType();
        }

        /**
         * 配置 OkHttpClient
         *
         * @param config 配置器
         * @return Builder
         */
        public Builder config(OkConfig config) {
            this.config = config;
            return this;
        }

        /**
         * 设置 baseUrl
         *
         * @param baseUrl 全局URL前缀
         * @return Builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * 配置媒体类型
         *
         * @param mediaTypes 媒体类型
         * @return Builder
         */
        public Builder mediaTypes(Map<String, String> mediaTypes) {
            if (mediaTypes != null) {
                this.mediaTypes.putAll(mediaTypes);
            }
            return this;
        }

        /**
         * 配置媒体类型
         *
         * @param key   媒体类型KEY
         * @param value 媒体类型VALUE
         * @return Builder
         */
        public Builder mediaTypes(String key, String value) {
            if (key != null && value != null) {
                this.mediaTypes.put(key, value);
            }
            return this;
        }

        /**
         * 设置回调执行器，例如实现切换线程功能，只对异步请求有效
         *
         * @param executor 回调执行器
         * @return Builder
         */
        public Builder callbackExecutor(Executor executor) {
            this.mainExecutor = executor;
            return this;
        }

        /**
         * 添加可并行处理请求任务的预处理器
         *
         * @param preprocessor 预处理器
         * @return Builder
         */
        public Builder addPreprocessor(Preprocessor preprocessor) {
            if (preprocessor != null) {
                preprocessors.add(preprocessor);
            }
            return this;
        }

        /**
         * 添加预处理器
         *
         * @param preprocessor 预处理器
         * @return Builder
         */
        public Builder addSerialPreprocessor(Preprocessor preprocessor) {
            if (preprocessor != null) {
                preprocessors.add(new HttpClient.SerialPreprocessor(preprocessor));
            }
            return this;
        }

        /**
         * 最大预处理时间（倍数，相当普通请求的超时时间）
         *
         * @param times 普通超时时间的倍数，默认为 10
         * @return Builder
         */
        public Builder preprocTimeoutTimes(int times) {
            if (times > 0) {
                this.preprocTimeoutTimes = times;
            }
            return this;
        }

        /**
         * 设置全局响应监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder responseListener(TaskListener<HttpResult> listener) {
            this.responseListener = listener;
            return this;
        }

        /**
         * 设置全局异常监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder exceptionListener(TaskListener<IOException> listener) {
            this.exceptionListener = listener;
            return this;
        }

        /**
         * 设置全局完成监听
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder completeListener(TaskListener<HttpResult.State> listener) {
            this.completeListener = listener;
            return this;
        }

        /**
         * 设置下载监听器
         *
         * @param listener 监听器
         * @return Builder
         */
        public Builder downloadListener(DownListener listener) {
            this.downloadListener = listener;
            return this;
        }

        /**
         * @since 2.0.0
         * 添加消息转换器
         * @param msgConvertor JSON 服务
         * @return Builder
         */
        public Builder addMsgConvertor(MsgConvertor msgConvertor) {
            if (msgConvertor != null) {
                this.msgConvertors.add(msgConvertor);
            }
            return this;
        }

        /**
         * @since 2.0.0
         * 设置默认编码格式
         * @param charset 编码
         * @return Builder
         */
        public Builder charset(Charset charset) {
            if (charset != null) {
                this.charset = charset;
            }
            return this;
        }

        /**
         * @since 2.0.0
         * 设置默认请求体类型
         * @param bodyType 请求体类型
         * @return Builder
         */
        public Builder bodyType(String bodyType) {
            if (bodyType != null) {
                this.bodyType = bodyType;
            }
            return this;
        }

        /**
         * 构建 HTTP 实例
         * @return HTTP
         */
        public HTTP build() {
            if (config != null || okClient == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                if (config != null) {
                    config.config(builder);
                }
                okClient = builder.build();
            }
            return new HttpClient(this);
        }

        public OkHttpClient okClient() {
            return okClient;
        }

        public String baseUrl() {
            return baseUrl;
        }

        public Map<String, String> getMediaTypes() {
            return mediaTypes;
        }

        public Executor mainExecutor() {
            return mainExecutor;
        }

        public Preprocessor[] preprocessors() {
            return preprocessors.toArray(new Preprocessor[0]);
        }

        public DownListener downloadListener() {
            return downloadListener;
        }

        public TaskListener<HttpResult> responseListener() {
            return responseListener;
        }

        public TaskListener<IOException> exceptionListener() {
            return exceptionListener;
        }

        public TaskListener<HttpResult.State> completeListener() {
            return completeListener;
        }

        public MsgConvertor[] msgConvertors() {
            return msgConvertors.toArray(new MsgConvertor[0]);
        }

        public int preprocTimeoutTimes() {
            return preprocTimeoutTimes;
        }

        public Charset charset() {
            return charset;
        }

        public String bodyType() {
            return bodyType;
        }

    }

}
