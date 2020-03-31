package com.ejlchina.okhttps.internal;

import java.io.IOException;

import com.ejlchina.okhttps.HttpCall;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.HttpResult.State;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * 异步 Http 请求任务
 *  
 * @author Troy.Zhou
 * 
 */
public class AsyncHttpTask extends HttpTask<AsyncHttpTask> {

	
    private OnCallback<HttpResult> onResponse;
    private OnCallback<IOException> onException;
    private OnCallback<State> onComplete;
    private boolean rOnIO;
    private boolean eOnIO;
    private boolean cOnIO;
    
    
	public AsyncHttpTask(HttpClient client, String url) {
		super(client, url);
	}


	/**
	 * 设置请求执行异常后的回调函数，设置后，相关异常将不再向上抛出
	 * @param onException 请求异常回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnException(OnCallback<IOException> onException) {
        this.onException = onException;
        eOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

	/**
	 * 设置请求执行完成后的回调函数，无论成功|失败|异常 都会被执行
	 * @param onComplete 请求完成回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnComplete(OnCallback<State> onComplete) {
        this.onComplete = onComplete;
        cOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResponse 请求响应回调
	 * @return HttpTask 实例
	 */
    public AsyncHttpTask setOnResponse(OnCallback<HttpResult> onResponse) {
        this.onResponse = onResponse;
        rOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
    /**
     * 发起 GET 请求
     * @return HttpCall
     */
    public HttpCall get() {
        return request("GET");
    }

    /**
     * 发起 POST 请求
     * @return HttpCall
     */
    public HttpCall post() {
        return request("POST");
    }

    /**
     * 发起 PUT 请求
     * @return HttpCall
     */
    public HttpCall put() {
        return request("PUT");
    }

    /**
     * 发起 DELETE 请求
     * @return HttpCall
     */
    public HttpCall delete() {
        return request("DELETE");
    }
    
    private HttpCall request(String method) {
    	PreHttpCall call = new PreHttpCall();
    	httpClient.preprocess(this, () -> {
    		synchronized (call) {
    			if (!call.isCanceled()) {
    				call.setCall(executeCall(prepareCall(method)));
        		}
			}
    	});
    	if (tag != null) {
    		httpClient.addTagCall(tag, call, this);
    	}
    	return call;
    }
    
    
    class PreHttpCall implements HttpCall {

    	private boolean canceled = false;
    	private HttpCall call;
    	
		@Override
		public boolean cancel() {
			boolean res = true;
			synchronized (this) {
				if (call != null) {
					res = call.cancel();
				} else {
					canceled = true;
				}
				notify();
			}
			if (tag != null && call == null) {
	    		httpClient.removeTagCall(AsyncHttpTask.this);
	    	}
			return res;
		}

		@Override
		public boolean isDone() {
			if (call != null) {
				return call.isDone();
			}
			return canceled;
		}

		@Override
		public boolean isCanceled() {
			if (call != null) {
				return call.isCanceled();
			}
			return canceled;
		}

		public void setCall(HttpCall call) {
			this.call = call;
			notify();
		}

		@Override
		public synchronized HttpResult getResult() {
			if (canceled) {
				return new RealHttpResult(AsyncHttpTask.this, State.CANCELED);
			}
			if (call == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new HttpException(e.getMessage(), e);
				}
			}
			if (call != null) {
				return call.getResult();
			}
			return new RealHttpResult(AsyncHttpTask.this, State.CANCELED);
		}

    }
    
    class OkHttpCall implements HttpCall {

    	private Call call;
    	private HttpResult result;
    	
		public OkHttpCall(Call call) {
			this.call = call;
		}

		@Override
		public boolean cancel() {
			if (result == null) {
				call.cancel();
				return true;
			}
			return false;
		}

		@Override
		public boolean isDone() {
			return result != null;
		}

		@Override
		public boolean isCanceled() {
			return call.isCanceled();
		}

		@Override
		public synchronized HttpResult getResult() {
			if (result == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new HttpException(e.getMessage(), e);
				}
			}
			return result;
		}

		public void setResult(HttpResult result) {
			synchronized (this) {
				this.result = result;
				notify();
			}
			if (tag != null) {
	    		httpClient.removeTagCall(AsyncHttpTask.this);
	    	}
		}

    }
	
    private HttpCall executeCall(Call call) {
        OkHttpCall httpCall = new OkHttpCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException error) {
            	State state = toState(error);
            	if (state == State.CANCELED) {
            		httpCall.setResult(new RealHttpResult(AsyncHttpTask.this, state));
            	} else {
            		TaskExecutor executor = httpClient.getExecutor();
            		executor.executeOnComplete(AsyncHttpTask.this, onComplete, state, cOnIO);
            		if (!executor.executeOnException(AsyncHttpTask.this, onException, error, eOnIO)
            				&& !nothrow) {
            			throw new HttpException(error.getMessage(), error);
            		}
            		httpCall.setResult(new RealHttpResult(AsyncHttpTask.this, state, error));
            	}
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            	TaskExecutor executor = httpClient.getExecutor();
            	HttpResult result = new RealHttpResult(AsyncHttpTask.this, response, executor);
        		executor.executeOnComplete(AsyncHttpTask.this, onComplete, State.RESPONSED, cOnIO);
        		executor.executeOnResponse(AsyncHttpTask.this, onResponse, result, rOnIO);
            	httpCall.setResult(result);
            }
			
        });
		return httpCall;
    }

}
