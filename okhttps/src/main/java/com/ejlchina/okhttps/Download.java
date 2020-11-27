package com.ejlchina.okhttps;

import com.ejlchina.okhttps.internal.HttpException;
import com.ejlchina.okhttps.internal.TaskExecutor;

import java.io.*;

/**
 * 文件下载
 * @author Troy.Zhou
 */
public class Download {
    
    private File file;
    private InputStream input;
    private OnCallback<File> onSuccess;
    private OnCallback<Failure> onFailure;
    private TaskExecutor taskExecutor;
    private long doneBytes;
    private int buffSize = 0;
    private long seekBytes = 0;
    private boolean appended;
    private volatile int status;
    private final Object lock = new Object();
    
    protected boolean nextOnIO = false;
    private boolean sOnIO;
    private boolean fOnIO;
    
    private Ctrl ctrl;
    
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
     * 设置下载失败回调
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
     * 开始下载
     * @return 下载控制器
     */
    public Ctrl start() {
        if (buffSize == 0) {
            buffSize = Process.DEFAULT_STEP_BYTES;
        }
        RandomAccessFile raFile = randomAccessFile();
        status = Ctrl.STATUS__DOWNLOADING;
        taskExecutor.execute(() -> doDownload(raFile), true);
        return ctrl;
    }
    
    /**
     * 获取下载控制器
     * @return Ctrl
     */
    public Ctrl getCtrl() {
        return ctrl;
    }

    public class Ctrl {
        
        /**
         * 已取消
         */
        public static final int STATUS__CANCELED = -1;
        
        /**
         * 下载中
         */
        public static final int STATUS__DOWNLOADING = 1;
        
        /**
         * 已暂停
         */
        public static final int STATUS__PAUSED = 2;
        
        /**
         * 已完成
         */
        public static final int STATUS__DONE = 3;
        
        /**
         * 错误
         */
        public static final int STATUS__ERROR = 4;
        
        /**
         * @see #STATUS__CANCELED
         * @see #STATUS__DOWNLOADING
         * @see #STATUS__PAUSED
         * @see #STATUS__DONE
         * @return 下载状态
         */
        public int status() {
            return status;
        }
        
        /**
         * 暂停下载任务
         */
        public void pause() {
            synchronized (lock) {
                if (status == STATUS__DOWNLOADING) {
                    status = STATUS__PAUSED;
                }
            }
        }
        
        /**
         * 继续下载任务
         */
        public void resume() {
            synchronized (lock) {
                if (status == STATUS__PAUSED) {
                    status = STATUS__DOWNLOADING;
                }
            }
        }
        
        /**
         * 取消下载任务
         */
        public void cancel() {
            synchronized (lock) {
                if (status == STATUS__PAUSED || status == STATUS__DOWNLOADING) {
                    status = STATUS__CANCELED;
                }
            }
        }
        
    }
    
    public class Failure {

        private IOException exception;

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
         * @return 已下载字节数
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
            status = Ctrl.STATUS__ERROR;
            closeQuietly(input);
            throw new HttpException("无法获取文件[" + file.getAbsolutePath() + "]的输入流", e);
        }
    }
    
    private void doDownload(RandomAccessFile raFile) {
        try {
            if (appended && seekBytes > 0) {
                long length = raFile.length();
                if (seekBytes <= length) {
                    raFile.seek(seekBytes);
                    doneBytes = seekBytes;
                } else {
                    raFile.seek(length);
                    doneBytes = length;
                }
            }
            while (status != Ctrl.STATUS__CANCELED && status != Ctrl.STATUS__DONE) {
                if (status == Ctrl.STATUS__DOWNLOADING) {
                    byte[] buff = new byte[buffSize];
                    int len;
                    while ((len = input.read(buff)) != -1) {
                        raFile.write(buff, 0, len);
                        doneBytes += len;
                        if (status == Ctrl.STATUS__CANCELED 
                                || status == Ctrl.STATUS__PAUSED) {
                            break;
                        }
                    }
                    if (len == -1) {
                        synchronized (lock) {
                            if (status != Ctrl.STATUS__CANCELED) {
                                status = Ctrl.STATUS__DONE;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            synchronized (lock) {
                status = Ctrl.STATUS__ERROR;
            }
            if (onFailure != null) {
                taskExecutor.execute(() -> {
                    onFailure.on(new Failure(e));
                }, fOnIO);
            } else {
                throw new HttpException("流传输失败", e);
            }
        } finally {
            closeQuietly(raFile);
            closeQuietly(input);
            if (status == Ctrl.STATUS__CANCELED) {
                file.delete();
            }
        }
        if (status == Ctrl.STATUS__DONE
                && onSuccess != null) {
            taskExecutor.execute(() -> {
                onSuccess.on(file);
            }, sOnIO);
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
