package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.HttpResult;

/**
 * Created by 周旭（Troy.Zhou） on 2016/8/30.
 */
public class HttpException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8223484833265657324L;

	private HttpResult.State state;

	public HttpException(String detailMessage) {
        super(detailMessage);
    }

    public HttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public HttpException(HttpResult.State state, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.state = state;
    }

    public HttpResult.State getState() {
        return state;
    }

}
