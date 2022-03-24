package com.ejlchina.okhttps.internal;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;

/**
 * 修复报文体在写出过程中会发生无法被异常回调捕获的非 IO 异常的问题
 * @author Troy.Zhou @ 2021-09-01
 * @since v3.1.5
 */
public class FixedRequestBody extends RequestBody {

    private final RequestBody requestBody;

    public FixedRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public boolean isDuplex() {
        return requestBody.isDuplex();
    }

    @Override
    public boolean isOneShot() {
        return requestBody.isOneShot();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(@SuppressWarnings("NullableProblems") BufferedSink sink) throws IOException {
        try {
            requestBody.writeTo(sink);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("请求体写出异常", e);
        }
    }

}
