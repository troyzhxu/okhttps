package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.AsyncHttpTask;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 并行下载器
 */
public class ParallelDownload {

    /**
     * 默认最大线程数：10
     */
    public static final int DEFAULT_MAX_THREADS = 10;

    /**
     * 默认每块基准大小：30 Mb
     */
    public static final int DEFAULT_PART_BYTES = 1024 * 1024 * 30;

    private final String url;
    private final int clientType;
    private final HTTP http;

    private int maxThreads = DEFAULT_MAX_THREADS;
    private int partBytes = DEFAULT_PART_BYTES;
    private boolean onlyGet = false;

    private boolean running = false;

    private File targetFile;                                    // 目标文件
    private final List<File> tempFiles = new ArrayList<>();     // 中间临时文件

    private int threads;    // 实际下载线程数
    private int doneCount;  // 已经下载完成的线程数

    private OnCallback<File> onSuccess;
    private OnCallback<ParallelProcess> onProcess;
    private OnCallback<ParallelFailure> onFailure;

    public ParallelDownload(String url, int clientType, HTTP http) {
        this.url = url;
        this.clientType = clientType;
        this.http = http;
    }

    /**
     * 指定每块下载大小（基准）
     * @param partBytes 分隔块数
     * @return ParallelDownload
     */
    public ParallelDownload partBytes(int partBytes) {
        if (running) {
            throw new IllegalStateException("can not call this method after start running");
        }
        this.partBytes = partBytes;
        return this;
    }

    /**
     * 指定分隔最大块数，即下载线程个数
     * @param maxThreads 分隔块数
     * @return ParallelDownload
     */
    public ParallelDownload maxThreads(int maxThreads) {
        if (running) {
            throw new IllegalStateException("can not call this method after start running");
        }
        this.maxThreads = maxThreads;
        return this;
    }

    /**
     * 指定服务器只支持 GET 请求
     * @return ParallelDownload
     */
    public ParallelDownload onlyGet() {
        if (running) {
            throw new IllegalStateException("can not call this method after start running");
        }
        this.onlyGet = true;
        return this;
    }

    public ParallelDownload toFile(String filePath) {
        return toFile(new File(filePath));
    }

    public ParallelDownload toFile(File file) {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent == null) {
                throw new IllegalStateException("不正确的下载路径：" + file.getPath());
            }
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("不能创建父目录：" + parent.getPath());
            }
            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException("文件刚被其它线程占用：" + parent.getPath());
                }
            } catch (IOException e) {
                throw new IllegalStateException("文件创建失败：" + file.getPath());
            }
        }
        targetFile = file;
        return this;
    }

    /**
     * 设置下载成功回调
     * @param onSuccess 成功回调函数
     * @return Download
     */
    public ParallelDownload setOnSuccess(OnCallback<File> onSuccess) {
        if (this.onSuccess != null) {
            throw new IllegalStateException("repeat call setOnSuccess method!");
        }
        this.onSuccess = onSuccess;
        return this;
    }

    /**
     * 设置下载进度回调
     * @param onProcess 进度回调
     * @return Download
     */
    public ParallelDownload setOnProcess(OnCallback<ParallelProcess> onProcess) {
        if (this.onProcess != null) {
            throw new IllegalStateException("repeat call setOnProcess method!");
        }
        this.onProcess = onProcess;
        return this;
    }

    /**
     * 设置失败回调
     * @param onFailure 失败回调
     * @return ParallelDownload
     */
    public ParallelDownload setOnFailure(OnCallback<ParallelFailure> onFailure) {
        if (this.onFailure != null) {
            throw new IllegalStateException("repeat call setOnFailure method!");
        }
        this.onFailure = onFailure;
        return this;
    }


    private HTTP getHttp() {
        if (http != null) {
            return http;
        }
        if (clientType == Parallel.OK_HTTPS) {
            return OkHttps.getHttp();
        }
        if (clientType == Parallel.HTTP_UTILS) {
            return HttpUtils.getHttp();
        }
        throw new IllegalStateException("clientType must be Parallel.OK_HTTPS(1) or Parallel.HTTP_UTILS(2), but: " + clientType);
    }

    /**
     * 开始下载
     */
    public void start() {
        AsyncHttpTask task = getHttp()
                .async(url)
                .setOnResponse(res -> {
                    if (res.isSuccessful()) {
                        start(res.close().getContentLength());
                    } else {
                        fireFailure("获取文件[" + url + "]长度失败：" + res.getStatus(), res);
                    }
                })
                .setOnException(e -> fireFailure("获取文件[" + url + "]长度异常", e));
        if (onlyGet) {
            task.get();
        } else {
            task.head();
        }
    }

    private void fireFailure(String message, IOException error) {
        if (onFailure != null) {
            onFailure.on(new ParallelFailure(message, error));
        }
    }

    private void fireFailure(String message, HttpResult res) {
        if (onFailure != null) {
            onFailure.on(new ParallelFailure(message, res));
        }
    }

    private void fireFailure(String message, Download.Failure failure) {
        if (onFailure != null) {
            onFailure.on(new ParallelFailure(message, failure));
        }
    }

    /**
     * 开始下载
     * @param totalBytes 原文件大小
     */
    public void start(long totalBytes) {
        if (targetFile == null) {
            throw new IllegalStateException("targetFile is null, you should call toFile method before start !");
        }
        if (running) {
            throw new IllegalStateException("can not start more times!");
        }
        running = true;
        // 下载线程数
        threads = (int) (totalBytes / partBytes);
        if (totalBytes % partBytes != 0) {
            threads ++;
        }
        threads = Math.min(threads, maxThreads);
        // 每个线程下载大小
        long size = totalBytes / threads;
        Platform.logInfo("Parallel - threads =" + threads + ", size = " + size);
        for (int i = 0; i < threads; i++) {
            int index = i;
            long start = size * i;
            long end = i == threads - 1 ? totalBytes : start + size;
            File partFile = resolvePartFile(index);
            tempFiles.add(partFile);
            new Thread(() -> download(partFile, index, start, end - 1)).start();
        }
    }

    private void download(File partFile, int index, long start, long end) {
        System.out.println("index = " + index + ", start = " + start + ", end = " + end);
        getHttp().async(url)
                .setRange(start, end)
                .setOnResponse(res -> {
                    if (res.isSuccessful()) {
                        download(partFile, index, res.getBody());
                    } else {
                        fireFailure("下载文件[" + url + "][" + index + "]失败：" + res.getStatus(), res);
                    }
                })
                .setOnException(e -> fireFailure("下载文件[" + url + "][" + index + "]异常", e))
                .get();
    }


    private synchronized int addDoneCount() {
        return ++doneCount;
    }


    private void download(File partFile, int index, HttpResult.Body body) {
        System.out.println("开始下载：" + index);
        if (onProcess != null) {
            body.setOnProcess(p -> onProcess.on(new ParallelProcess(index, threads, p)));
        }
        body.toFile(partFile)
                .setOnSuccess(f -> {
                    System.out.println("下载成功：" + index);
                    if (addDoneCount() == threads) {
                        try {
                            mergeFile();
                        } catch (Exception e) {
                            throw new IllegalStateException("下载文件合并失败：" + url);
                        }
                    }
                })
                .setOnFailure(f -> fireFailure("下载文件[" + url + "][" + index + "]失败", f))
                .start();
    }

    private File resolvePartFile(int index) {
        File partFile = null;
        int i = 0;
        while (partFile == null) {
            partFile = new File(targetFile.getPath() + index + "-" + i + ".part");
            if (partFile.exists()) {
                partFile = null;
                i++;
                continue;
            }
            try {
                boolean res = partFile.createNewFile();
                if (!res) {
                    Platform.logError("Parallel [" + url + "][" + index + "] - file exists: " + partFile.getPath());
                }
            } catch (IOException e) {
                throw new IllegalStateException("文件创建失败：" + partFile.getPath());
            }
        }
        return partFile;
    }

    private void mergeFile() throws Exception {
        OutputStream output = new RafOutputStream(targetFile);
        for (File file : tempFiles) {
            Files.copy(file.toPath(), output);
            if (!file.delete()) {
                Platform.logError("Parallel [" + url + "] tempFile [" + file.toPath() + "] can not be deleted!");
            }
        }
        output.close();
        if (onSuccess != null) {
            onSuccess.on(targetFile);
        }
    }

}
