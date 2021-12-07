package com.ejlchina.okhttps;

import com.ejlchina.okhttps.HttpResult.State;
import okhttp3.MediaType;
import okhttp3.internal.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class TaskExecutor {

    private final Executor ioExecutor;
    private final Executor mainExecutor;
    private final DownListener downloadListener;
    private final TaskListener<HttpResult> responseListener;
    private final TaskListener<IOException> exceptionListener;
    private final TaskListener<State> completeListener;
    private final MsgConvertor[] msgConvertors;
    private final String[] contentTypes;
    private Scheduler taskScheduler;
    
    public TaskExecutor(HTTP.Builder builder, Executor ioExecutor) {
        this.downloadListener = builder.downloadListener();
        this.responseListener = builder.responseListener();
        this.exceptionListener = builder.exceptionListener();
        this.completeListener = builder.completeListener();
        this.msgConvertors = builder.msgConvertors();
        this.taskScheduler = builder.taskScheduler();
        this.contentTypes = builder.contentTypes();
        this.mainExecutor = builder.mainExecutor();
        this.ioExecutor = ioExecutor;
    }

    public Executor getExecutor(boolean onIo) {
        if (onIo || mainExecutor == null) {
            return ioExecutor;
        }
        return mainExecutor;
    }

    public Download download(HttpTask<?> httpTask, File file, InputStream input, long skipBytes) {
        Download download = new Download(file, input, this, skipBytes);
        if (httpTask != null && downloadListener != null) {
            downloadListener.listen(httpTask, download);
        }
        return download;
    }
    
    public void execute(Runnable command, boolean onIo) {
        Executor executor = ioExecutor;
        if (mainExecutor != null && !onIo) {
            executor = mainExecutor;
        }
        executor.execute(command);
    }
    
    public void executeOnResponse(HttpTask<?> task, AHttpTask.OkHttpCall call, OnCallback<HttpResult> onResponse, HttpResult result, boolean onIo) {
        Runnable runnable = () -> {
            if (!call.isCanceled()) onResponse.on(result);
        };
        if (responseListener != null) {
            if (responseListener.listen(task, result) && onResponse != null) {
                execute(runnable, onIo);
            } else {
                call.finish();
            }
        } else if (onResponse != null) {
            execute(runnable, onIo);
        } else {
            result.close();
            call.finish();
        }
    }

    public boolean executeOnException(HttpTask<?> task, AHttpTask.OkHttpCall call, OnCallback<IOException> onException, IOException error, boolean onIo) {
        Runnable runnable = () -> {
            if (!call.isCanceled()) {
                onException.on(error);
            }
            call.finish();
        };
        if (exceptionListener != null) {
            if (exceptionListener.listen(task, error) && onException != null) {
                execute(runnable, onIo);
            } else {
                call.finish();
            }
        } else if (onException != null) {
            execute(runnable, onIo);
        } else {
            call.finish();
            return false;
        }
        return true;
    }
    
    public void executeOnComplete(HttpTask<?> task, OnCallback<State> onComplete, State state, boolean onIo) {
        if (completeListener != null) {
            if (completeListener.listen(task, state) && onComplete != null) {
                execute(() -> onComplete.on(state), onIo);
            }
        } else if (onComplete != null) {
            execute(() -> onComplete.on(state), onIo);
        }
    }


    public interface ConvertFunc<T> {

        T apply(MsgConvertor convertor);

    }

    public static class Data<T> {

        public final T data;
        private final String contentType;

        public Data(T data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        public MediaType mediaType(Charset charset) {
            if (contentType != null) {
                return MediaType.parse(contentType.replace("{charset}", charset.name()));
            }
            return null;
        }

    }

    public <V> V doMsgConvert(ConvertFunc<V> callable) {
        return doMsgConvert(null, callable).data;
    }

    public <V> Data<V> doMsgConvert(String type, ConvertFunc<V> callable) {
        Throwable cause = null;
        for (int i = msgConvertors.length - 1; i >= 0; i--) {
            MsgConvertor convertor = msgConvertors[i];
            String mediaType = convertor.mediaType();
            if (type != null && (mediaType == null || !mediaType.toLowerCase().contains(type))) {
                continue;
            }
            if (callable == null && mediaType != null) {
                return new Data<>(null, mediaType(type, mediaType));
            }
            try {
                assert callable != null;
                return new Data<>(callable.apply(convertor), mediaType(type, mediaType));
            } catch (Exception e) {
                if (cause != null) {
                    initRootCause(e, cause);
                }
                cause = e;
            }
        }
        if (callable == null) {
        	return new Data<>(null, toContentType(type));
        }
        if (cause != null) {
            throw new OkHttpsException("转换失败", cause);
        }
        throw new OkHttpsException("没有匹配[" + type + "]类型的转换器！");
    }

    private void initRootCause(Throwable throwable, Throwable cause) {
        Throwable lastCause = throwable.getCause();
        if (lastCause != null) {
            initRootCause(lastCause, cause);
        } else {
            throwable.initCause(cause);
        }
    }

    private String mediaType(String type, String mediaType) {
        return type != null && type.contains("/") ? type : mediaType;
    }

    private String toContentType(String type) {
    	if (type != null) {
    	    if (type.contains("/")) {
    	        return type;
            }
    		for (String contentType : contentTypes) {
    		    if (contentType.contains(type)) {
    		        return contentType;
                }
            }
    	}
    	return type;
    }

    private ScheduledExecutorService scheduledService;

    public synchronized Scheduler requireScheduler() {
        if (taskScheduler == null) {
            scheduledService = new ScheduledThreadPoolExecutor(1, Util.threadFactory("OkHttps-Scheduler", false));
            taskScheduler = scheduledService::schedule;
        }
        return taskScheduler;
    }

    /**
     * 关闭线程池
     * @since OkHttps V1.0.2
     */
    public void shutdown() {
        if (ioExecutor != null && ioExecutor instanceof ExecutorService) {
            ((ExecutorService) ioExecutor).shutdown();
        }
        if (mainExecutor != null && mainExecutor instanceof ExecutorService) {
            ((ExecutorService) mainExecutor).shutdown();
        }
        if (scheduledService != null) {
            scheduledService.shutdown();
        }
    }

    public Executor getIoExecutor() {
        return ioExecutor;
    }

    public Executor getMainExecutor() {
        return mainExecutor;
    }

    public DownListener getDownloadListener() {
        return downloadListener;
    }

    public TaskListener<HttpResult> getResponseListener() {
        return responseListener;
    }

    public TaskListener<IOException> getExceptionListener() {
        return exceptionListener;
    }

    public TaskListener<State> getCompleteListener() {
        return completeListener;
    }

    public MsgConvertor[] getMsgConvertors() {
        return msgConvertors;
    }

    public Scheduler getTaskScheduler() {
        return taskScheduler;
    }

    public String[] getContentTypes() {
        return contentTypes;
    }

    public boolean isMulitMsgConvertor() {
        return msgConvertors.length > 1;
    }

}
