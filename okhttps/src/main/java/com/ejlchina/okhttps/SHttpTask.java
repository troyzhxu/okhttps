package com.ejlchina.okhttps;

import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.AbstractHttpClient;
import com.ejlchina.okhttps.internal.RealHttpResult;
import okhttp3.Call;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


/**
 * 同步 Http 请求任务
 *  
 * @author Troy.Zhou
 * 
 */
public class SHttpTask extends HttpTask<SHttpTask> {

	public SHttpTask(AbstractHttpClient httpImpl, String url) {
		super(httpImpl, url);
	}


	@Override
	public boolean isSyncHttp() {
		return true;
	}

	/**
     * 发起 GET 请求（Rest：获取资源，幂等）
     * @return 请求结果  
     */
    public HttpResult get() {
        return request(HTTP.GET);
    }

	/**
	 * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
	 * @return 请求结果
	 */
	public HttpResult head() {
		return request(HTTP.HEAD);
	}

    /**
     * 发起 POST 请求（Rest：创建资源，非幂等）
     * @return 请求结果  
     */
    public HttpResult post() {
        return request(HTTP.POST);
    }

    /**
     * 发起 PUT 请求（Rest：更新资源，幂等）
     * @return 请求结果  
     */
    public HttpResult put() {
        return request(HTTP.PUT);
    }

	/**
	 * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
	 * @return HttpCall
	 */
	public HttpResult patch() {
		return request(HTTP.PATCH);
	}

    /**
     * 发起 DELETE 请求（Rest：删除资源，幂等）
     * @return 请求结果  
     */
    public HttpResult delete() {
        return request(HTTP.DELETE);
    }

    /**
     * 发起 HTTP 请求
     * @param method 请求方法
     * @return 请求结果  
     */
    public HttpResult request(String method) {
    	if (method == null || method.isEmpty()) {
    		throw new IllegalArgumentException("HTTP 请求方法 method 不可为空！");
    	}
    	RealHttpResult result = new RealHttpResult(this, httpClient.executor());
		SyncHttpCall httpCall = new SyncHttpCall();
		// 注册标签任务
		registeTagTask(httpCall);
		CountDownLatch latch = new CountDownLatch(1);
    	httpClient.preprocess(this, () -> {
			synchronized (httpCall) {
				if (httpCall.canceled) {
					result.exception(State.CANCELED, null);
					latch.countDown();
					return;
				}
				httpCall.call = prepareCall(method);
			}
            try {
				result.response(httpCall.call.execute());
				httpCall.done = true;
            } catch (IOException e) {
				result.exception(toState(e), e);
            } finally {
				latch.countDown();
			}
    	}, skipPreproc, skipSerialPreproc);
    	boolean timeout = false;
		if (result.getState() == null) {
			timeout = !timeoutAwait(latch);
		}
		// 移除标签任务
		removeTagTask();
		if (timeout) {
			httpCall.cancel();
			return timeoutResult();
		}
		IOException e = result.getError();
		State state = result.getState();
    	if (e != null && state != State.CANCELED
    			&& !nothrow) {
    		throw new OkHttpsException(state, "同步请求异常：" + getUrl(), e);
    	}
        return result;
    }
    

    static class SyncHttpCall implements Cancelable {

		Call call;
		boolean done = false;
		boolean canceled = false;

		@Override
		public synchronized boolean cancel() {
			if (done) {
				return false;
			}
			if (call != null) {
				call.cancel();
			}
			canceled = true;
			return true;
		}

	}

}
