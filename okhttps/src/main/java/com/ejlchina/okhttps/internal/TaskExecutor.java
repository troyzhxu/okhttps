package com.ejlchina.okhttps.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.HttpResult.State;
import okhttp3.internal.Util;

public final class TaskExecutor {

    private Executor ioExecutor;
    private Executor mainExecutor;
    private Scheduler taskScheduler;
    private DownListener downloadListener;
    private TaskListener<HttpResult> responseListener;
    private TaskListener<IOException> exceptionListener;
    private TaskListener<State> completeListener;
    private MsgConvertor[] msgConvertors;
    
    public TaskExecutor(Executor ioExecutor, Executor mainExecutor, DownListener downloadListener,
            TaskListener<HttpResult> responseListener, TaskListener<IOException> exceptionListener, 
            TaskListener<State> completeListener, MsgConvertor[] msgConvertors,
                        Scheduler taskScheduler) {
        this.ioExecutor = ioExecutor;
        this.mainExecutor = mainExecutor;
        this.downloadListener = downloadListener;
        this.responseListener = responseListener;
        this.exceptionListener = exceptionListener;
        this.completeListener = completeListener;
        this.msgConvertors = msgConvertors;
        this.taskScheduler = taskScheduler;
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
    
    public void executeOnResponse(HttpTask<?> task, OnCallback<HttpResult> onResponse, HttpResult result, boolean onIo) {
        if (responseListener != null) {
            if (responseListener.listen(task, result) && onResponse != null) {
                execute(() -> onResponse.on(result), onIo);
            }
        } else if (onResponse != null) {
            execute(() -> onResponse.on(result), onIo);
        } else {
            result.close();
        }
    }

    public boolean executeOnException(HttpTask<?> task, OnCallback<IOException> onException, IOException error, boolean onIo) {
        if (exceptionListener != null) {
            if (exceptionListener.listen(task, error) && onException != null) {
                execute(() -> onException.on(error), onIo);
            }
        } else if (onException != null) {
            execute(() -> onException.on(error), onIo);
        } else {
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

        public T data;
        public String mediaType;

        public Data(T data, String mediaType) {
            this.data = data;
            this.mediaType = mediaType;
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
            if (type != null && (mediaType == null || !mediaType.contains(type))) {
                continue;
            }
            if (callable == null && mediaType != null) {
                return new Data<>(null, mediaType);
            }
            try {
                assert callable != null;
                return new Data<>(callable.apply(convertor), mediaType);
            } catch (Exception e) {
                if (cause != null) {
                    initRootCause(e, cause);
                }
                cause = e;
            }
        }
        if (callable == null) {
            return new Data<>(null, "application/x-www-form-urlencoded");
        }
        if (cause != null) {
            throw new HttpException("转换失败", cause);
        }

        throw new HttpException("没有匹配[" + type + "]类型的转换器！");
    }

    private void initRootCause(Throwable throwable, Throwable cause) {
        Throwable lastCause = throwable.getCause();
        if (lastCause != null) {
            initRootCause(lastCause, cause);
        } else {
            throwable.initCause(cause);
        }
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

}
