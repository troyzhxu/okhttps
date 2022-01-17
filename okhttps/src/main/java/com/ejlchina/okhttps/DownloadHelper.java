package com.ejlchina.okhttps;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 下载助手，用户解析下载路径与文件名
 */
public class DownloadHelper {

    /**
     * 解析下载文件名
     * @param result HTTP 响应结果
     * @return 文件名
     */
    public String resolveFileName(HttpResult result) {
        String fileName = result.getHeader("Content-Disposition");
        // 通过 Content-Disposition 获取文件名，这点跟服务器有关，需要灵活变通
        if (fileName == null || fileName.length() < 1) {
            fileName = result.getTask().getUrl();
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else {
            fileName = URLDecoder.decode(fileName.substring(
                    fileName.indexOf("filename=") + 9), StandardCharsets.UTF_8);
            // 有些文件名会被包含在""里面，所以要去掉，不然无法读取文件后缀
            fileName = fileName.replaceAll("\"", "");
        }
        return fileName;
    }



}
