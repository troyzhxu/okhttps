package cn.zhxu.okhttps;

import cn.zhxu.okhttps.internal.StreamRequestBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @since 2.5.0 从 HttpTask 中分离
 */
public class FilePara {

    private final String type;
    private final String fileName;
    private final byte[] content;
    private final File file;
    private final InputStream stream;

    public FilePara(String type, String fileName, byte[] content) {
        this(type, fileName, null, content, null);
    }

    public FilePara(String type, String fileName, File file) {
        this(type, fileName, file, null, null);
    }

    public FilePara(String type, String fileName, InputStream stream) {
        this(type, fileName, null, null, stream);
    }

    private FilePara(String type, String fileName, File file, byte[] content, InputStream stream) {
        this.type = type;
        this.fileName = fileName;
        this.file = file;
        this.content = content;
        this.stream = stream;
    }

    public RequestBody toRequestBody(MediaType contentType) {
        if (file != null) {
            return RequestBody.create(contentType, file);
        }
        if (content != null) {
            return RequestBody.create(contentType, content);
        }
        if (stream != null) {
            return new StreamRequestBody(contentType, stream);
        }
        throw new IllegalStateException("Invalid FilePara");
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

    public InputStream getStream() {
        return stream;
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
