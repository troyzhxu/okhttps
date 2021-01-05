package com.ejlchina.okhttps;

/**
 * 并行下载
 * 把同一个文件用多线程下载到不同的临时文件内，最后合并
 */
public class Parallel {

    /**
     * 使用 OkHttps 工具类
     */
    public static final int OK_HTTPS = 1;

    /**
     * 使用 HttpUtils 工具类
     */
    public static final int HTTP_UTILS = 2;

    /**
     * 使用 HTTP 实例并行下载
     * @param http 使用的 http 实例
     * @param url 下载地址
     * @return 并行下载器
     */
    public static ParallelDownload download(HTTP http, String url) {
        return new ParallelDownload(url, 0, http);
    }

    /**
     * 使用 工具类 并行下载
     * @see #OK_HTTPS
     * @see #HTTP_UTILS
     * @param type 使用的工具类类型
     * @param url 下载地址
     * @return 并行下载器
     */
    public static ParallelDownload download(int type, String url) {
        if (type != OK_HTTPS && type != HTTP_UTILS) {
            throw new IllegalArgumentException("type must be Parallel.OK_HTTPS(1) or Parallel.HTTP_UTILS(2), but: " + type);
        }
        return new ParallelDownload(url, type, null);
    }

}
