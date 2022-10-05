package cn.zhxu.okhttps.internal;

import cn.zhxu.okhttps.*;
import com.ejlchina.okhttps.*;
import okhttp3.MediaType;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public abstract class AbstractHttpClient implements HTTP {

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
    // 最大预处理时间倍数（相对于普通请求的超时时间）
    final int preprocTimeoutTimes;
    // 编码格式
    final Charset charset;
    // 默认的请求体类型
    final String bodyType;
    // 下载助手
    final DownloadHelper downloadHelper;

    public AbstractHttpClient(HTTP.Builder builder) {
        this.baseUrl = builder.baseUrl();
        this.mediaTypes = builder.getMediaTypes();
        this.executor = new TaskExecutor(builder, ioExecutor(builder));
        this.preprocessors = builder.preprocessors();
        this.preprocTimeoutTimes = builder.preprocTimeoutTimes();
        this.charset = builder.charset();
        this.bodyType = builder.bodyType();
        this.tagTasks = new LinkedList<>();
        this.downloadHelper = builder.downloadHelper();
    }


    public abstract Executor ioExecutor(HTTP.Builder builder);


    @Override
    public AHttpTask async(String url) {
        return new AHttpTask(this, urlPath(url, false));
    }

    @Override
    public SHttpTask sync(String url) {
        return new SHttpTask(this, urlPath(url, false));
    }

    @Override
    public WHttpTask webSocket(String url) {
        return new WHttpTask(this, urlPath(url, true));
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
        doCancelAll();
        synchronized (tagTasks) {
            tagTasks.clear();
        }
    }

    public abstract void doCancelAll();

    public int preprocTimeoutMillis() {
        return preprocTimeoutTimes * totalTimeoutMillis();
    }

    public abstract int totalTimeoutMillis();

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

    public class TagTask {

        String tag;
        Cancelable canceler;
        HttpTask<?> task;
        long createAt;

        TagTask(String tag, Cancelable canceler, HttpTask<?> task) {
            this.tag = tag;
            this.canceler = canceler;
            this.task = task;
            this.createAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            // 生存时间大于10倍的总超时限值
            return System.currentTimeMillis() - createAt > preprocTimeoutMillis();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

    }

    public MediaType mediaType(String type) {
        String mediaType = mediaTypes.get(type);
        if (mediaType != null) {
            return MediaType.parse(mediaType);
        }
        if (type != null) {
            if (type.indexOf('/') < 0) {
                type = "application/" + type;
            }
            MediaType mType = MediaType.parse(type);
            if (mType != null) {
                return mType;
            }
        }
        return MediaType.parse("application/unknown");
    }

    @Override
    public TaskExecutor executor() {
        return executor;
    }


    public void preprocess(HttpTask<?> httpTask, Runnable request,
                           boolean skipPreproc, boolean skipSerialPreproc) {
        if (preprocessors.length == 0 || skipPreproc) {
            request.run();
            return;
        }
        int index = 0;
        if (skipSerialPreproc) {
            while (index < preprocessors.length
                    && preprocessors[index] instanceof SerialPreprocessor) {
                index++;
            }
        }
        if (index < preprocessors.length) {
            RealPreChain chain = new RealPreChain(preprocessors,
                    httpTask, request, index + 1,
                    skipSerialPreproc);
            preprocessors[index].doProcess(chain);
        } else {
            request.run();
        }
    }


    class RealPreChain implements Preprocessor.PreChain {

        private int index;
        final Preprocessor[] preprocessors;
        final HttpTask<?> httpTask;
        final Runnable request;
        final boolean noSerialPreprocess;

        public RealPreChain(Preprocessor[] preprocessors, HttpTask<?> httpTask, Runnable request,
                            int index, boolean noSerialPreprocess) {
            this.index = index;		// index 大于等于 1
            this.preprocessors = preprocessors;
            this.httpTask = httpTask;
            this.request = request;
            this.noSerialPreprocess = noSerialPreprocess;
        }

        @Override
        public HttpTask<?> getTask() {
            return httpTask;
        }

        @Override
        public HTTP getHttp() {
            return AbstractHttpClient.this;
        }

        @Override
        public void proceed() {
            if (noSerialPreprocess) {
                while (index < preprocessors.length
                        && preprocessors[index] instanceof SerialPreprocessor) {
                    index++;
                }
            } else {
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

    private String urlPath(String urlPath, boolean websocket) {
        String fullUrl;
        if (urlPath == null) {
            if (baseUrl != null) {
                fullUrl = baseUrl;
            } else {
                throw new OkHttpsException("在设置 BaseUrl 之前，您必须指定具体路径才能发起请求！");
            }
        } else {
            urlPath = urlPath.trim();
            boolean isFullPath = urlPath.startsWith("https://")
                    || urlPath.startsWith("http://")
                    || urlPath.startsWith("wss://")
                    || urlPath.startsWith("ws://");
            if (isFullPath) {
                fullUrl = urlPath;
            } else if (baseUrl != null) {
                fullUrl = baseUrl + urlPath;
            } else {
                throw new OkHttpsException("在设置 BaseUrl 之前，您必须使用全路径URL发起请求，当前URL为：'" + urlPath + "'");
            }
        }
        if (websocket && fullUrl.startsWith("http")) {
            return fullUrl.replaceFirst("http", "ws");
        }
        if (!websocket && fullUrl.startsWith("ws")) {
            return fullUrl.replaceFirst("ws", "http");
        }
        return fullUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Map<String, String> mediaTypes() {
        return mediaTypes;
    }

    public Preprocessor[] preprocessors() {
        return preprocessors;
    }

    public List<TagTask> tagTasks() {
        return tagTasks;
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

    public DownloadHelper downloadHelper() {
        return downloadHelper;
    }

}
