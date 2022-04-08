package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.MulVMap;
import com.ejlchina.okhttps.TaskExecutor;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class RealHttpResult implements HttpResult {

    private State state;
    private Response response;
    private IOException error;
    private TaskExecutor taskExecutor;
    private final HttpTask<?> httpTask;
    private Body body;
    
    public RealHttpResult(HttpTask<?> httpTask, State state) {
        this.httpTask = httpTask;
        this.state = state;
    }
    
    public RealHttpResult(HttpTask<?> httpTask, Response response, TaskExecutor taskExecutor) {
        this(httpTask, taskExecutor);
        response(response);
    }
    
    public RealHttpResult(HttpTask<?> httpTask, TaskExecutor taskExecutor) {
        this.httpTask = httpTask;
        this.taskExecutor = taskExecutor;
    }
    
    public RealHttpResult(HttpTask<?> httpTask, State state, IOException error) {
        this.httpTask = httpTask;
        exception(state, error);
    }

    public void exception(State state, IOException error) {
        this.state = state;
        this.error = error;
    }
    
    public void response(Response response) {
        this.state = State.RESPONSED;
        this.response = response;
    }
    
    @Override
    public State getState() {
        return state;
    }

    @Override
    public int getStatus() {
        if (response != null) {
            return response.code();
        }
        return 0;
    }

    @Override
    public boolean isSuccessful() {
        if (response != null) {
            return response.isSuccessful();
        }
        return false;
    }
    
    @Override
    public Headers getHeaders() {
        if (response != null) {
            return response.headers();
        }
        return null;
    }

    @Override
    public MulVMap<String> getAllHeaders() {
        MulVMap<String> map = new MulVMap<>();
        if (response != null) {
            Headers hs = response.headers();
            for (int i = 0, size = hs.size(); i < size; i++) {
                map.put(hs.name(i), hs.value(i));
            }
        }
        return map;
    }

    @Override
    public List<String> getHeaders(String name) {
        if (response != null) {
            return response.headers(name);
        }
        return Collections.emptyList();
    }

    @Override
    public String getHeader(String name) {
        if (response != null) {
            return response.header(name);
        }
        return null;
    }

    @Override
    public long getContentLength() {
        String length = getHeader("Content-Length");
        if (length != null) {
            try {
                return Long.parseLong(length);
            } catch (Exception ignore) {}
        }
        return 0;
    }

    @Override
    public synchronized Body getBody() {
        if (body == null && response != null) {
            body = new ResultBody(this, response, taskExecutor);
        }
        return body;
    }

    @Override
    public HttpTask<?> getTask() {
        return httpTask;
    }

    @Override
    public IOException getError() {
        return error;
    }

    public Response getResponse() {
        return response;
    }

    @Override
    public String toString() {
        Body body = getBody();
        String str = "HttpResult [\n  state: " + state + ",\n  status: " + getStatus() 
                + ",\n  headers: " + getHeaders();
        if (body != null) {
            str += ",\n  contentType: " + body.getType();
        }
        return str + ",\n  error: " + error + "\n]";
    }

    @Override
    public HttpResult close() {
        if (response != null) {
            response.close();
        }
        return this;
    }

}
