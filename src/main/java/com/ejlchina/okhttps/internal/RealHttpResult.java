package com.ejlchina.okhttps.internal;

import java.util.Collections;
import java.util.List;

import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;

import okhttp3.Headers;
import okhttp3.Response;


public class RealHttpResult implements HttpResult {

    private State state;
    private Response response;
    private Exception error;
    private TaskExecutor taskExecutor;
    private HttpTask<?> httpTask;
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
    
    public RealHttpResult(HttpTask<?> httpTask, State state, Exception error) {
        this.httpTask = httpTask;
        exception(state, error);
    }
    
    
    public void exception(State state, Exception error) {
        this.state = state;
        this.error = error;
    }
    
    public void response(Response response) {
        this.state = State.RESPONSED;
        this.response = response;
    }
    
    @Override
    public HttpTask<?> getTask() {
        return httpTask;
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
    public Body getBody() {
        if (body == null && response != null) {
            body = new ResultBody(httpTask, response, taskExecutor);
        }
        return body;
    }
    
    @Override
    public Exception getError() {
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
            str += ",\n  contentType: " + body.getContentType();
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
