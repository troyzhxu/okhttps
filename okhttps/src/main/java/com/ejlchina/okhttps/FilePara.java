package com.ejlchina.okhttps;

import java.io.File;

/**
 * @since 2.5.0 从 HttpTask 中分离
 */
public class FilePara {

    String type;
    String fileName;
    byte[] content;
    File file;

    public FilePara(String type, String fileName, byte[] content) {
        this.type = type;
        this.fileName = fileName;
        this.content = content;
    }

    public FilePara(String type, String fileName, File file) {
        this.type = type;
        this.fileName = fileName;
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "FilePara{" + "type='" + type + "', fileName='" + fileName + "'}";
    }

}
