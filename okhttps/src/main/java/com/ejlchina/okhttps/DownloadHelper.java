package com.ejlchina.okhttps;

import okhttp3.MediaType;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载助手，用户解析下载路径与文件名
 */
public class DownloadHelper {

    private Map<String, String> extMappings = new HashMap<>();

    public DownloadHelper() {
        extMappings.put("video/mpeg4", "mp4");
        extMappings.put("text/plain", "txt");
        extMappings.put("application/x-xls", "text/xls");
        extMappings.put("application/vnd.android.package-archive", "apk");
        extMappings.put("application/msword", "doc");
    }

    /**
     * 解析下载文件名
     * @param result HTTP 响应结果
     * @return 文件名
     */
    public String resolveFileName(HttpResult result) {
        String contentDisposition = result.getHeader("Content-Disposition");
        // 通过 Content-Disposition 获取文件名，这点跟服务器有关，需要灵活变通
        if (contentDisposition == null || contentDisposition.length() < 1) {
            String urlPath = result.getTask().getUrl();
            String urlName = urlPath.substring(urlPath.lastIndexOf("/") + 1);
            return toFileName(urlName, result);
        } else {
            String filename = URLDecoder.decode(contentDisposition.substring(
                    contentDisposition.indexOf("filename=") + 9), StandardCharsets.UTF_8);
            // 有些文件名会被包含在""里面，所以要去掉，不然无法读取文件后缀
            return filename.replaceAll("\"", "");
        }
    }

    protected String toFileName(String name, HttpResult result) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > -1 && dotIndex < name.length() - 1) {
            return name;
        }
        MediaType type = result.getBody().getType();
        // 获取文件扩展名
        String ext = extMappings.get(type.toString());
        if (ext == null) {
            ext = type.subtype();
        }
        if (dotIndex == -1) {
            return name + "." + ext;
        }
        return name + ext;
    }

    /**
     * 解析文件路径
     * @param dirPath 目录
     * @param fileName 文件名
     * @return 文件路径
     */
    public String resolveFilePath(String dirPath, String fileName) {
        if (dirPath.endsWith("\\") || dirPath.endsWith("/")) {
            return dirPath + fileName;
        }
        return dirPath + File.separator + fileName;
    }

    /**
     * 当文件已存在时，根据该方法生成一个新的文件名
     * @param fileName 原文件名
     * @param index 重复次数
     * @return 新的文件名
     */
    public String indexFileName(String fileName, int index) {
        int i = fileName.lastIndexOf('.');
        if (i < 0) {
            return fileName + "(" + index + ")";
        }
        String ext = fileName.substring(i);
        if (i > 0) {
            String name = fileName.substring(0, i);
            return name + "(" + index + ")" + ext;
        }
        return "(" + index + ")" + ext;
    }

    public Map<String, String> getExtMappings() {
        return extMappings;
    }

    public void setExtMappings(Map<String, String> extMappings) {
        this.extMappings = extMappings;
    }

}
