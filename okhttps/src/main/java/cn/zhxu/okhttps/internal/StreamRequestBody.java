package cn.zhxu.okhttps.internal;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;

/**
 * 流式请求体
 * @author Troy.Zhou @ 2021-09-01
 * @since v3.5.0
 */
public class StreamRequestBody extends RequestBody {

    private final MediaType contentType;
    private final InputStream inputStream;

    public StreamRequestBody(MediaType contentType, InputStream inputStream) {
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        try {
            sink.writeAll(Okio.source(inputStream));
        } finally {
            inputStream.close();
        }
    }

    @Override
    public boolean isOneShot() {
        return true;
    }

}
