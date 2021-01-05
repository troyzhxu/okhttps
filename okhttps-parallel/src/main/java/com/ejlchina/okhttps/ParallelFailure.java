package com.ejlchina.okhttps;

import java.io.IOException;

public class ParallelFailure {

    private String message;
    private HttpResult result;
    private IOException error;
    private Download.Failure failure;

    public ParallelFailure(String message, IOException error) {
        this.message = message;
        this.error = error;
    }

    public ParallelFailure(String message, HttpResult result) {
        this.message = message;
        this.result = result;
    }

    public ParallelFailure(String message, Download.Failure failure) {
        this.message = message;
        this.failure = failure;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public IOException getError() {
        return error;
    }

    public void setError(IOException error) {
        this.error = error;
    }

    public HttpResult getResult() {
        return result;
    }

    public void setResult(HttpResult result) {
        this.result = result;
    }

    public Download.Failure getFailure() {
        return failure;
    }

    public void setFailure(Download.Failure failure) {
        this.failure = failure;
    }

}
