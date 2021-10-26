package com.ejlchina.okhttps.okhttp;

import com.ejlchina.okhttps.internal.AbstractHttpImpl;
import okhttp3.*;

import java.util.concurrent.Executor;


public class OkHttpImpl extends AbstractHttpImpl {

    // OkHttpClient
    final OkHttpClient okClient;

    public OkHttpImpl(OkHttpBuilderImpl builder) {
        super(builder);
        this.okClient = builder.okClient();
    }

    @Override
    public Executor ioExecutor(Builder builder) {
        OkHttpBuilderImpl builderImpl = (OkHttpBuilderImpl) builder;
        return builderImpl.okClient().dispatcher().executorService();
    }

    @Override
    public void doCancelAll() {
        okClient.dispatcher().cancelAll();
    }

    @Override
    public Call request(Request request) {
        return okClient.newCall(request);
    }

    @Override
    public WebSocket webSocket(Request request, WebSocketListener listener) {
        return okClient.newWebSocket(request, listener);
    }

    public OkHttpClient okClient() {
        return okClient;
    }

    @Override
    public int totalTimeoutMillis() {
        return okClient.connectTimeoutMillis() + okClient.writeTimeoutMillis() + okClient.readTimeoutMillis();
    }

    @Override
    public Builder newBuilder() {
        return new OkHttpBuilderImpl(this);
    }

}
