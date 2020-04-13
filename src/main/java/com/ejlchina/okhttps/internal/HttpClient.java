package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.HttpResult.State;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;


public class HttpClient implements HTTP {

    // OkHttpClient
    final OkHttpClient client;
    // 根URL
    final String baseUrl;
    // 媒体类型
    final Map<String, String> mediaTypes;
    // 执行器
    final TaskExecutor executor;
    // 预处理器
    final Preprocessor[] preprocessors;
    // 持有标签的任务
    final List<TagTask> tagTasks;


    private HttpClient(Builder builder) {
        this.client = builder.client;
        this.baseUrl = builder.baseUrl;
        this.mediaTypes = builder.mediaTypes;
        this.executor = new TaskExecutor(client.dispatcher().executorService(),
                builder.mainExecutor, builder.downloadListener,
                builder.responseListener, builder.exceptionListener,
                builder.completeListener);
        this.preprocessors = builder.preprocessors.toArray(new Preprocessor[0]);
        this.tagTasks = new LinkedList<>();
    }

    @Override
    public AsyncHttpTask async() {
        return async(null);
    }

    @Override
    public AsyncHttpTask async(String url) {
        return new AsyncHttpTask(this, urlPath(url));
    }

    @Override
    public SyncHttpTask sync() {
        return sync(null);
    }

    @Override
    public SyncHttpTask sync(String url) {
        return new SyncHttpTask(this, urlPath(url));
    }

    @Override
    public int cancel(String tag) {
        if (tag == null) {
            return 0;
        }
        int count = 0;
        synchronized (tagTasks) {
        	Iterator<TagTask> it = tagTasks.iterator();
            while (it.hasNext()) {
                TagTask tagCall = it.next();
                // 只要任务的标签包含指定的Tag就会被取消
                if (tagCall.tag.contains(tag)) {
                    if (tagCall.canceler.cancel()) {
                        count++;
                    }
                    it.remove();
                } else if (tagCall.isExpired()) {
                    it.remove();
                }
            }
        }
        return count;
    }

    @Override
    public void cancelAll() {
        client.dispatcher().cancelAll();
        synchronized (tagTasks) {
        	tagTasks.clear();
        }
    }

    @Override
    public Call request(Request request) {
        return client.newCall(request);
    }

    @Override
    public WebSocket webSocket(Request request, WebSocketListener listener) {
        return client.newWebSocket(request, listener);
    }

    public OkHttpClient getOkClient() {
        return client;
    }

    public int totalTimeoutMillis() {
        return client.connectTimeoutMillis() + client.writeTimeoutMillis() + client.readTimeoutMillis();
    }

    public int getTagTaskCount() {
        return tagTasks.size();
    }

    public TagTask addTagTask(String tag, Cancelable canceler, HttpTask<?> task) {
        TagTask tagTask = new TagTask(tag, canceler, task);
        synchronized (tagTasks) {
        	tagTasks.add(tagTask);
        }
		return tagTask;
    }

    public void removeTagTask(HttpTask<?> task) {
    	synchronized (tagTasks) {
    		Iterator<TagTask> it = tagTasks.iterator();
            while (it.hasNext()) {
                TagTask tagCall = it.next();
                if (tagCall.task == task) {
                    it.remove();
                    break;
                }
                if (tagCall.isExpired()) {
                    it.remove();
                }
            }
    	}
    }

    class TagTask {

        String tag;
        Cancelable canceler;
        HttpTask<?> task;
        long createAt;

        TagTask(String tag, Cancelable canceler, HttpTask<?> task) {
            this.tag = tag;
            this.canceler = canceler;
            this.task = task;
            this.createAt = System.nanoTime();
        }

        boolean isExpired() {
            // 生存时间大于10倍的总超时限值
            return System.nanoTime() - createAt > 10_000_000 * totalTimeoutMillis();
        }

    }

    public MediaType getMediaType(String type) {
        String mediaType = mediaTypes.get(type);
        if (mediaType != null) {
            return MediaType.parse(mediaType);
        }
        return MediaType.parse("application/octet-stream");
    }

    public TaskExecutor getExecutor() {
        return executor;
    }

    public void preprocess(HttpTask<? extends HttpTask<?>> httpTask, Runnable request) {
        if (preprocessors.length > 0) {
            RealPreChain chain = new RealPreChain(preprocessors,
                    httpTask, request);
            preprocessors[0].doProcess(chain);
        } else {
            request.run();
        }
    }

    /**
     * 串行预处理器
     * @author Troy.Zhou
     */
    static class SerialPreprocessor implements Preprocessor {

        // 预处理器
        private Preprocessor preprocessor;
        // 待处理的任务队列
        private Queue<PreChain> pendings;
        // 是否有任务正在执行
        private boolean running = false;

        SerialPreprocessor(Preprocessor preprocessor) {
            this.preprocessor = preprocessor;
            this.pendings = new LinkedList<>();
        }

        @Override
        public void doProcess(PreChain chain) {
            boolean should = true;
            synchronized (this) {
                if (running) {
                    pendings.add(chain);
                    should = false;
                } else {
                    running = true;
                }
            }
            if (should) {
                preprocessor.doProcess(chain);
            }
        }

        public void afterProcess() {
            PreChain chain = null;
            synchronized (this) {
                if (pendings.size() > 0) {
                    chain = pendings.poll();
                } else {
                    running = false;
                }
            }
            if (chain != null) {
                preprocessor.doProcess(chain);
            }
        }

    }


    class RealPreChain implements Preprocessor.PreChain {

        private int index;

        private Preprocessor[] preprocessors;

        private HttpTask<?> httpTask;

        private Runnable request;

        public RealPreChain(Preprocessor[] preprocessors,
                            HttpTask<?> httpTask,
                            Runnable request) {
            this.index = 1;
            this.preprocessors = preprocessors;
            this.httpTask = httpTask;
            this.request = request;
        }

        @Override
        public HttpTask<?> getTask() {
            return httpTask;
        }

        @Override
        public HTTP getHttp() {
            return HttpClient.this;
        }

        @Override
        public void proceed() {
            if (index > 0) {
                Preprocessor last = preprocessors[index - 1];
                if (last instanceof SerialPreprocessor) {
                    ((SerialPreprocessor) last).afterProcess();
                }
            }
            if (index < preprocessors.length) {
                preprocessors[index++].doProcess(this);
            } else {
                request.run();
            }
        }

    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    private String urlPath(String urlPath) {
        if (urlPath == null) {
            if (baseUrl != null) {
                return baseUrl;
            }
            throw new HttpException("在设置 BaseUrl 之前，您必须指定具体路径才能发起请求！");
        }
        boolean isFullPath = urlPath.startsWith("https://")
                || urlPath.startsWith("http://");
        if (isFullPath) {
            return urlPath;
        }
        if (baseUrl != null) {
            return baseUrl + urlPath;
        }
        throw new HttpException("在设置 BaseUrl 之前，您必须使用全路径URL发起请求，当前URL为：" + urlPath);
    }

    public static class Builder {

        private OkHttpClient client;

        private String baseUrl;

        private Map<String, String> mediaTypes;

        private Configurator configurator;

        private Executor mainExecutor;

        private List<Preprocessor> preprocessors;

        private DownListener downloadListener;

        private TaskListener<HttpResult> responseListener;

        private TaskListener<IOException> exceptionListener;

        private TaskListener<State> completeListener;

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
        }

        private Builder(HttpClient hc) {
            this.client = hc.client;
            this.baseUrl = hc.baseUrl;
            this.mediaTypes = hc.mediaTypes;
            this.preprocessors = new ArrayList<>();
            Collections.addAll(this.preprocessors, hc.preprocessors);
        }

        /**
         * 配置 OkHttpClient
         *
         * @param configurator 配置器
         * @return Builder
         */
        public Builder config(Configurator configurator) {
            this.configurator = configurator;
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
            this.mediaTypes.putAll(mediaTypes);
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
            this.mediaTypes.put(key, value);
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
            preprocessors.add(preprocessor);
            return this;
        }

        /**
         * 添加预处理器
         *
         * @param preprocessor 预处理器
         * @return Builder
         */
        public Builder addSerialPreprocessor(Preprocessor preprocessor) {
            preprocessors.add(new SerialPreprocessor(preprocessor));
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
        public Builder completeListener(TaskListener<State> listener) {
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

        public HTTP build() {
            if (configurator != null || client == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                if (configurator != null) {
                    configurator.config(builder);
                }
                client = builder.build();
            }
            return new HttpClient(this);
        }

    }

}
