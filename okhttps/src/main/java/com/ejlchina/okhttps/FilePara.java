package com.ejlchina.okhttps;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @since 2.5.0 从 HttpTask 中分离
 */
public class FilePara {

    final String type;
    final String fileName;
    final byte[] content;
    final File file;
    final InputStream stream;

    public FilePara(String type, String fileName, byte[] content) {
        this(type, fileName, null, content, null);
    }

    public FilePara(String type, String fileName, File file) {
        this(type, fileName, file, null, null);
    }

    public FilePara(String type, String fileName, InputStream stream) {
        this(type, fileName, null, null, stream);
    }

    public FilePara(String type, String fileName, File file, byte[] content, InputStream stream) {
        this.type = type;
        this.fileName = fileName;
        this.file = file;
        this.content = content;
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "FilePara{" +
                "type='" + type + '\'' +
                ", fileName='" + fileName + '\'' +
                ", content=" + Arrays.toString(content) +
                ", file=" + file +
                ", stream=" + stream +
                '}';
    }

}
