package cn.zhxu.okhttps.okhttp;

import cn.zhxu.okhttps.HTTP;
import cn.zhxu.okhttps.internal.AbstractHttpClient;
import okhttp3.*;

import java.util.concurrent.Executor;


public class OkHttpClientWrapper extends AbstractHttpClient {

    // OkHttpClient
    final OkHttpClient okClient;

    public OkHttpClientWrapper(OkHttpBuilderImpl builder) {
        super(builder);
        this.okClient = builder.okClient();
    }

    @Override
    public Executor ioExecutor(HTTP.Builder builder) {
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
    public HTTP.Builder newBuilder() {
        return new OkHttpBuilderImpl(this);
    }

}
