package com.ejlchina.okhttps.okhttp;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.internal.*;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;


public class OkHttpBuilderImpl implements HTTP.Builder {

    private OkHttpClient okClient;

    private String baseUrl;

    private final List<HTTP.OkConfig> configs;

    private final Map<String, String> mediaTypes;

    private final List<String> contentTypes;

    private final List<MsgConvertor> msgConvertors;

    private final List<Preprocessor> preprocessors;

    private int preprocTimeoutTimes = 10;

    private Executor mainExecutor;

    private Scheduler taskScheduler;

    private DownListener downloadListener;

    private TaskListener<HttpResult> responseListener;

    private TaskListener<IOException> exceptionListener;

    private TaskListener<HttpResult.State> completeListener;

    private Charset charset = StandardCharsets.UTF_8;

    private String bodyType = OkHttps.FORM;

    private DownloadHelper downloadHelper;

    public OkHttpBuilderImpl() {
        mediaTypes = new HashMap<>();
        mediaTypes.put("png", "image/png");
        mediaTypes.put("jpg", "image/jpeg");
        mediaTypes.put("jpeg", "image/jpeg");
        mediaTypes.put("wav", "audio/wav");
        mediaTypes.put("mp3", "audio/mp3");
        mediaTypes.put("mp4", "video/mp4");
        mediaTypes.put("txt", "text/plain");
        mediaTypes.put("xls", "application/x-xls");
        mediaTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mediaTypes.put("xml", "text/xml");
        mediaTypes.put("apk", "application/vnd.android.package-archive");
        mediaTypes.put("doc", "application/msword");
        mediaTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mediaTypes.put("html", "text/html");
        mediaTypes.put("rar", "application/x-rar");
        mediaTypes.put("jar", "application/x-java-archive");
        contentTypes = new ArrayList<>();
        contentTypes.add("application/x-www-form-urlencoded; charset={charset}");
        contentTypes.add("application/json; charset={charset}");
        contentTypes.add("application/xml; charset={charset}");
        contentTypes.add("application/protobuf");
        contentTypes.add("application/msgpack");
        preprocessors = new ArrayList<>();
        msgConvertors = new ArrayList<>();
        configs = new ArrayList<>();
        downloadHelper = new DownloadHelper();
    }

    public OkHttpBuilderImpl(OkHttpClientWrapper hc) {
        okClient = hc.okClient();
        baseUrl = hc.baseUrl();
        mediaTypes = hc.mediaTypes();
        preprocessors = new ArrayList<>();
        Collections.addAll(preprocessors, hc.preprocessors());
        TaskExecutor executor = hc.executor();
        contentTypes = new ArrayList<>();
        Collections.addAll(contentTypes, executor.getContentTypes());
        mainExecutor = executor.getMainExecutor();
        taskScheduler = executor.getTaskScheduler();
        downloadListener = executor.getDownloadListener();
        responseListener = executor.getResponseListener();
        exceptionListener = executor.getExceptionListener();
        completeListener = executor.getCompleteListener();
        msgConvertors = new ArrayList<>();
        Collections.addAll(msgConvertors, executor.getMsgConvertors());
        preprocTimeoutTimes = hc.preprocTimeoutTimes();
        configs = new ArrayList<>();
        bodyType = hc.bodyType();
        charset = hc.charset();
        downloadHelper = hc.downloadHelper();
    }


    public HTTP.Builder config(HTTP.OkConfig config) {
        if (config != null) {
            configs.add(config);
        }
        return this;
    }

    public HTTP.Builder baseUrl(String baseUrl) {
        if (baseUrl != null) {
            this.baseUrl = baseUrl.trim();
        }
        return this;
    }

    public HTTP.Builder mediaTypes(Map<String, String> mediaTypes) {
        if (mediaTypes != null) {
            this.mediaTypes.putAll(mediaTypes);
        }
        return this;
    }

    public HTTP.Builder mediaTypes(String key, String value) {
        if (key != null && value != null) {
            this.mediaTypes.put(key, value);
        }
        return this;
    }

    public HTTP.Builder clearContentTypes() {
        contentTypes.clear();
        return this;
    }

    public HTTP.Builder contentTypes(List<String> contentTypes) {
        if (contentTypes != null) {
            this.contentTypes.addAll(contentTypes);
        }
        return this;
    }

    public HTTP.Builder contentTypes(String contentType) {
        if (contentType != null) {
            this.contentTypes.add(contentType);
        }
        return this;
    }

    public HTTP.Builder callbackExecutor(Executor executor) {
        this.mainExecutor = executor;
        return this;
    }

    public HTTP.Builder taskScheduler(Scheduler scheduler) {
        this.taskScheduler = scheduler;
        return this;
    }

    public HTTP.Builder addPreprocessor(Preprocessor preprocessor) {
        if (preprocessor != null) {
            preprocessors.add(preprocessor);
        }
        return this;
    }

    public HTTP.Builder addSerialPreprocessor(Preprocessor preprocessor) {
        if (preprocessor != null) {
            preprocessors.add(new SerialPreprocessor(preprocessor));
        }
        return this;
    }

    public HTTP.Builder clearPreprocessors() {
        preprocessors.clear();
        return this;
    }

    public HTTP.Builder preprocTimeoutTimes(int times) {
        if (times > 0) {
            preprocTimeoutTimes = times;
        }
        return this;
    }

    public HTTP.Builder responseListener(TaskListener<HttpResult> listener) {
        responseListener = listener;
        return this;
    }

    public HTTP.Builder exceptionListener(TaskListener<IOException> listener) {
        exceptionListener = listener;
        return this;
    }

    public HTTP.Builder completeListener(TaskListener<HttpResult.State> listener) {
        completeListener = listener;
        return this;
    }

    public HTTP.Builder downloadListener(DownListener listener) {
        downloadListener = listener;
        return this;
    }

    public HTTP.Builder addMsgConvertor(MsgConvertor msgConvertor) {
        if (msgConvertor != null) {
            msgConvertors.add(msgConvertor);
        }
        return this;
    }

    public HTTP.Builder clearMsgConvertors() {
        msgConvertors.clear();
        return this;
    }

    public HTTP.Builder charset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
        return this;
    }

    public HTTP.Builder bodyType(String bodyType) {
        if (bodyType != null) {
            this.bodyType = bodyType.toLowerCase();
        }
        return this;
    }

    public HTTP build() {
        if (configs.size() > 0 || okClient == null) {
            OkHttpClient.Builder builder;
            if (okClient != null) {
                builder = okClient.newBuilder();
            } else {
                builder = new OkHttpClient.Builder();
            }
            for (HTTP.OkConfig config: configs) {
                config.config(builder);
            }
            // fix issue: https://github.com/ejlchina/okhttps/issues/8
            if (needCopyInterceptor(builder.interceptors())) {
                builder.addInterceptor(new CopyInterceptor());
            }
            okClient = builder.build();
        } else if (needCopyInterceptor(okClient.interceptors())) {
            okClient = okClient.newBuilder()
                    .addInterceptor(new CopyInterceptor())
                    .build();
        }
        return new OkHttpClientWrapper(this);
    }

    private boolean needCopyInterceptor(List<Interceptor> list) {
        return mainExecutor != null && Platform.ANDROID_SDK_INT > 24 && CopyInterceptor.notIn(list);
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

    public Scheduler taskScheduler() {
        return taskScheduler;
    }

    public String[] contentTypes() {
        return contentTypes.toArray(new String[0]);
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

    @Override
    public HTTP.Builder downloadHelper(DownloadHelper downloadHelper) {
        this.downloadHelper = downloadHelper;
        return this;
    }

    @Override
    public DownloadHelper downloadHelper() {
        return downloadHelper;
    }

}
