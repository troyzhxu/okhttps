package com.ejlchina.okhttps.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.ejlchina.okhttps.DownListener;
import com.ejlchina.okhttps.Download;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.TaskListener;
import com.ejlchina.okhttps.HttpResult.State;
import okhttp3.MediaType;

public class TaskExecutor {

    private Executor ioExecutor;
    private Executor mainExecutor;
    private DownListener downloadListener;
    private TaskListener<HttpResult> responseListener;
    private TaskListener<IOException> exceptionListener;
    private TaskListener<State> completeListener;
    private MsgConvertor msgConvertor;
    
    public TaskExecutor(Executor ioExecutor, Executor mainExecutor, DownListener downloadListener, 
            TaskListener<HttpResult> responseListener, TaskListener<IOException> exceptionListener, 
            TaskListener<State> completeListener, MsgConvertor msgConvertor) {
        this.ioExecutor = ioExecutor;
        this.mainExecutor = mainExecutor;
        this.downloadListener = downloadListener;
        this.responseListener = responseListener;
        this.exceptionListener = exceptionListener;
        this.completeListener = completeListener;
        this.msgConvertor = msgConvertor;
    }

    public Executor getExecutor(boolean onIoThread) {
        if (onIoThread || mainExecutor == null) {
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
    
    public void execute(Runnable command, boolean onIoThread) {
        Executor executor = ioExecutor;
        if (mainExecutor != null && !onIoThread) {
            executor = mainExecutor;
        }
        executor.execute(command);
    }
    
    public void executeOnResponse(HttpTask<?> task, OnCallback<HttpResult> onResponse, HttpResult result, boolean onIoThread) {
        if (responseListener != null) {
            execute(() -> {
                if (responseListener.listen(task, result) && onResponse != null) {
                    onResponse.on(result);
                }
            }, onIoThread);
        } else if (onResponse != null) {
            execute(() -> { onResponse.on(result); }, onIoThread);
        }
    }

    public boolean executeOnException(HttpTask<?> task, OnCallback<IOException> onException, IOException error, boolean onIoThread) {
        if (exceptionListener != null) {
            execute(() -> {
                if (exceptionListener.listen(task, error) && onException != null) {
                    onException.on(error);
                }
            }, onIoThread);
            return true;
        } else if (onException != null) {
            execute(() -> { onException.on(error); }, onIoThread);
            return true;
        }
        return false;
    }
    
    public void executeOnComplete(HttpTask<?> task, OnCallback<State> onComplete, State state, boolean onIoThread) {
        if (completeListener != null) {
            execute(() -> {
                if (completeListener.listen(task, state) && onComplete != null) {
                    onComplete.on(state);
                }
            }, onIoThread);
        } else if (onComplete != null) {
            execute(() -> { onComplete.on(state); }, onIoThread);
        }
    }

    public MsgConvertor convertor() {
    	if (msgConvertor != null) {
    		return msgConvertor;
    	}
    	throw new IllegalStateException("没有设置 MsgConvertor，不可做转换操作！");
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

    public MsgConvertor getMsgConvertor() {
        return msgConvertor;
    }

}
