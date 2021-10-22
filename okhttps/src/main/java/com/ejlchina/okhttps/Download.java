package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.HttpException;
import com.ejlchina.okhttps.internal.TaskExecutor;

import java.io.*;

/**
 * 文件下载
 * @author Troy.Zhou
 */
public class Download {
    
    private final File file;
    private final InputStream input;
    private final TaskExecutor taskExecutor;
    private final Ctrl ctrl;

    private OnCallback<File> onSuccess;
    private OnCallback<Failure> onFailure;
    private OnCallback<Status> onComplete;
    private long doneBytes;
    private int buffSize = 0;
    private long seekBytes = 0;
    private boolean appended;
    private volatile Status status;
    private final Object lock = new Object();
    
    protected boolean nextOnIO = false;
    private boolean sOnIO;
    private boolean fOnIO;
    private boolean cOnIO;
    
    public Download(File file, InputStream input, TaskExecutor taskExecutor, long skipBytes) {
        this.file = file;
        this.input = input;
        this.taskExecutor = taskExecutor;
        this.seekBytes = skipBytes;
        this.ctrl = new Ctrl();
    }

    /**
     * 设置缓冲区大小，默认 2K（2048）
     * @param buffSize 缓冲区大小（单位：字节）
     * @return Download
     */
    public Download setBuffSize(int buffSize) {
        if (buffSize > 0) {
            this.buffSize = buffSize;
        }
        return this;
    }
    
    /**
     * 设置文件追加模式
     * 用预断点续传和分块下载
     * @return Download
     */
    public Download setAppended() {
        this.appended = true;
        return this;
    }
    
    /**
     * 设置文件指针，从文件的 seekBytes 位置追加内容
     * 只有配合 setAppended() 方法一起才会有作用
     * @param seekBytes 跨越的字节数
     * @return Download
     */
    public Download setFilePointer(long seekBytes) {
        this.seekBytes = seekBytes;
        return this;
    }
    
    /**
     * 在IO线程执行
     * @return Download
     */
    public Download nextOnIO() {
        nextOnIO = true;
        return this;
    }
    
    /**
     * 设置下载成功回调
     * @param onSuccess 成功回调函数
     * @return Download
     */
    public Download setOnSuccess(OnCallback<File> onSuccess) {
        this.onSuccess = onSuccess;
        sOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
    /**
     * 设置下载失败回调（取消不执行）
     * @param onFailure 失败回调函数
     * @return Download
     */
    public Download setOnFailure(OnCallback<Failure> onFailure) {
        this.onFailure = onFailure;
        fOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * @since 3.2.0
     * 设置下载结束回调（成功、失败、取消都执行）
     * @param onComplete 结束回调函数
     * @return Download
     */
    public Download setOnComplete(OnCallback<Status> onComplete) {
        this.onComplete = onComplete;
        cOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 开始下载
     * @return 下载控制器
     */
    public Ctrl start() {
        if (buffSize == 0) {
            buffSize = Process.DEFAULT_STEP_BYTES;
        }
        RandomAccessFile raFile = randomAccessFile();
        if (raFile != null) {
            status = Status.DOWNLOADING;
            taskExecutor.execute(() -> doDownload(raFile), true);
        }
        return ctrl;
    }
    
    /**
     * 获取下载控制器
     * @return Ctrl
     */
    public Ctrl getCtrl() {
        return ctrl;
    }

    /**
     * 下载状态
     * @since v3.2.0
     */
    public enum Status {

        /**
         * 已取消
         */
        CANCELED(-1),

        /**
         * 下载中
         */
        DOWNLOADING(1),

        /**
         * 已暂停
         */
        PAUSED(2),

        /**
         * 成功下载完成
         */
        DONE(3),

        /**
         * 发送错误
         */
        ERROR(4);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

    }

    /**
     * 下载控制器
     */
    public class Ctrl {

        /**
         * @return 下载状态（v3.2.0 之前方法 返回整型常量，v3.2.0 开始修改为枚举）
         */
        public Status status() {
            return status;
        }
        
        /**
         * 暂停下载任务（只有处于下载中状态才能暂停成功）
         * @return 是否暂停成功
         */
        public boolean pause() {
            synchronized (lock) {
                if (status == Status.DOWNLOADING) {
                    status = Status.PAUSED;
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 继续下载任务（只有处于暂停状态才能恢复成功）
         * @return 是否恢复成功
         */
        public boolean resume() {
            synchronized (lock) {
                if (status == Status.PAUSED) {
                    status = Status.DOWNLOADING;
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 取消下载任务（只有处于暂停或下载中状态才能取消成功）
         * @return 是否取消成功
         */
        public boolean cancel() {
            synchronized (lock) {
                if (status == Status.PAUSED || status == Status.DOWNLOADING) {
                    status = Status.CANCELED;
                    return true;
                }
            }
            return false;
        }
        
    }
    
    public class Failure {

        private final IOException exception;

        Failure(IOException exception) {
            this.exception = exception;
        }
        
        /**
         * @return 下载文件
         */
        public File getFile() {
            return file;
        }
        
        /**
         * @return 本次已下载字节数
         */
        public long getDoneBytes() {
            return doneBytes;
        }

        /**
         * @return 异常信息
         */
        public IOException getException() {
            return exception;
        }
        
    }
    
    private RandomAccessFile randomAccessFile() {
        try {
            return new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            status = Status.ERROR;
            closeQuietly(input);
            fireOnComplete();
            fireOnFailure(e);
        }
        return null;
    }
    
    private void doDownload(RandomAccessFile raFile) {
        try {
            if (appended && seekBytes > 0) {
                // 使支持并行下载到同一个文件
                raFile.seek(seekBytes);
            }
            while (status != Status.CANCELED && status != Status.DONE) {
                if (status == Status.DOWNLOADING) {
                    byte[] buff = new byte[buffSize];
                    int len;
                    while ((len = input.read(buff)) != -1) {
                        raFile.write(buff, 0, len);
                        doneBytes += len;
                        if (status == Status.CANCELED
                                || status == Status.PAUSED) {
                            break;
                        }
                    }
                    if (len == -1) {
                        synchronized (lock) {
                            if (status != Status.CANCELED) {
                                status = Status.DONE;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            synchronized (lock) {
                if (status != Status.CANCELED) {
                    status = Status.ERROR;
                }
            }
            if (status == Status.ERROR) {
                fireOnFailure(e);
            }
        } finally {
            closeQuietly(raFile);
            closeQuietly(input);
            if (status == Status.CANCELED && !file.delete()) {
                Platform.logError("can not delete canceled file: " + file);
            }
            fireOnComplete();
        }
        if (status == Status.DONE) {
            fireOnSuccess();
        }
    }

    private void fireOnComplete() {
        OnCallback<Status> onComplete = this.onComplete;
        if (onComplete != null) {
            taskExecutor.execute(() -> onComplete.on(status), cOnIO);
        }
    }

    private void fireOnSuccess() {
        OnCallback<File> onSuccess = this.onSuccess;
        if (onSuccess != null) {
            taskExecutor.execute(() -> onSuccess.on(file), sOnIO);
        }
    }

    private void fireOnFailure(IOException e) {
        OnCallback<Failure> onFailure = this.onFailure;
        if (onFailure != null) {
            taskExecutor.execute(() -> onFailure.on(new Failure(e)), fOnIO);
        } else {
            throw new HttpException("Download failed: ", e);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

}
