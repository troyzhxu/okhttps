package com.ejlchina.http.internal;

/**
 * Created by 周旭（Troy.Zhou） on 2016/8/30.
 */
public class HttpException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8223484833265657324L;

	public HttpException(String detailMessage) {
        super(detailMessage);
    }

    public HttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

}
