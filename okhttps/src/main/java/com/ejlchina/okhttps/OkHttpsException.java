package com.ejlchina.okhttps;

/**
 * Created by 周旭（Troy.Zhou） on 2016/8/30.
 */
public class OkHttpsException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8223484833265657324L;

	private HttpResult.State state;

	public OkHttpsException(String detailMessage) {
        super(detailMessage);
    }

    public OkHttpsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public OkHttpsException(HttpResult.State state, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.state = state;
    }

    public OkHttpsException(HttpResult.State state, String detailMessage) {
        super(detailMessage);
        this.state = state;
    }
    
    public HttpResult.State getState() {
        return state;
    }

}
