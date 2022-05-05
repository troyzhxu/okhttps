package com.ejlchina.okhttps;

import com.ejlchina.data.Array;
import com.ejlchina.data.Mapper;
import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.AbstractHttpClient;
import com.ejlchina.okhttps.internal.CopyInterceptor;
import com.ejlchina.okhttps.internal.RealHttpResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * 异步 Http 请求任务
 *  
 * @author Troy.Zhou
 * 
 */
public class AHttpTask extends HttpTask<AHttpTask> {


    private Consumer<HttpResult> onResponse;
    private Consumer<IOException> onException;
    private Consumer<State> onComplete;

    private boolean responseOnIO;
    private boolean exceptionOnIO;
    private boolean completeOnIO;

    private Consumer<HttpResult.Body> onResBody;
    private Consumer<Mapper> onResMapper;
    private Consumer<Array> onResArray;
    private Consumer<String> onResString;
    private Consumer<?> onResBean;
    private Consumer<?> onResList;

    private boolean resBodyOnIO;
    private boolean resMapperOnIO;
    private boolean resArrayOnIO;
    private boolean resStringOnIO;
    private boolean resBeanOnIO;
    private boolean resListOnIO;

    private Type beanType;
	private Class<?> listType;

	public AHttpTask(AbstractHttpClient client, String url) {
		super(client, url);
	}


	@Override
	public boolean isAsyncHttp() {
		return true;
	}

	/**
	 * 设置请求执行异常后的回调函数，设置后，相关异常将不再向上抛出
	 * @param onException 请求异常回调
	 * @return HttpTask 实例
	 */
    public AHttpTask setOnException(Consumer<IOException> onException) {
        this.onException = onException;
        exceptionOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

	/**
	 * 设置请求执行完成后的回调函数，无论成功|失败|异常 都会被执行
	 * @param onComplete 请求完成回调
	 * @return HttpTask 实例
	 */
    public AHttpTask setOnComplete(Consumer<State> onComplete) {
        this.onComplete = onComplete;
        completeOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResponse 请求响应回调
	 * @return HttpTask 实例
	 */
    public synchronized AHttpTask setOnResponse(Consumer<HttpResult> onResponse) {
        this.onResponse = onResponse;
        responseOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResBody 响应报文体回调
	 * @return HttpTask 实例
	 */
    public synchronized AHttpTask setOnResBody(Consumer<HttpResult.Body> onResBody) {
    	this.onResBody = onResBody;
    	resBodyOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param <T> 泛型
	 * @param type 期望的转换类型
	 * @param onResBean 响应 Bean 回调
	 * @return HttpTask 实例
	 */
    public synchronized <T> AHttpTask setOnResBean(Class<T> type, Consumer<T> onResBean) {
    	initBeanType(type);
    	this.onResBean = onResBean;
    	resBeanOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

	/**
	 * 设置请求得到响应后的回调函数
	 * @param <T> 泛型
	 * @param type 期望的转换类型
	 * @param onResBean 响应 Bean 回调
	 * @return HttpTask 实例
	 */
	public synchronized <T> AHttpTask setOnResBean(TypeRef<T> type, Consumer<T> onResBean) {
		initBeanType(type.getType());
		this.onResBean = onResBean;
		resBeanOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 设置请求得到响应后的回调函数
	 * @param <T> 泛型
	 * @param type 期望的转换类型
	 * @param onResList 请求响应回调
	 * @return HttpTask 实例
	 */
    public synchronized <T> AHttpTask setOnResList(Class<T> type, Consumer<List<T>> onResList) {
		if (type == null) {
			throw new IllegalArgumentException(" list type can not be null!");
		}
    	listType = type;
    	this.onResList = onResList;
    	resListOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    

    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResMapper 请求响应回调
	 * @return HttpTask 实例
	 */
    public synchronized AHttpTask setOnResMapper(Consumer<Mapper> onResMapper) {
    	this.onResMapper = onResMapper;
    	resMapperOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResArray 请求响应回调
	 * @return HttpTask 实例
	 */
    public synchronized AHttpTask setOnResArray(Consumer<Array> onResArray) {
    	this.onResArray = onResArray;
    	resArrayOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
	/**
	 * 设置请求得到响应后的回调函数
	 * @param onResString 请求响应回调
	 * @return HttpTask 实例
	 */
    public synchronized AHttpTask setOnResString(Consumer<String> onResString) {
    	this.onResString = onResString;
    	resStringOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }
    
    /**
     * 发起 GET 请求（Rest：读取资源，幂等）
     * @return HttpCall
     */
    public HttpCall get() {
        return request(HTTP.GET);
    }

	/**
	 * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
	 * @return HttpCall
	 */
	public HttpCall head() {
		return request(HTTP.HEAD);
	}

    /**
     * 发起 POST 请求（Rest：创建资源，非幂等）
     * @return HttpCall
     */
    public HttpCall post() {
        return request(HTTP.POST);
    }

    /**
     * 发起 PUT 请求（Rest：更新资源，幂等）
     * @return HttpCall
     */
    public HttpCall put() {
        return request(HTTP.PUT);
    }

	/**
	 * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
	 * @return HttpCall
	 */
	public HttpCall patch() {
		return request(HTTP.PATCH);
	}

    /**
     * 发起 DELETE 请求（Rest：删除资源，幂等）
     * @return HttpCall
     */
    public HttpCall delete() {
        return request(HTTP.DELETE);
    }
    
    /**
     * 发起 HTTP 请求
     * @param method 请求方法
     * @return HttpCall
     */
    public HttpCall request(String method) {
    	if (method == null || method.isEmpty()) {
    		throw new IllegalArgumentException("HTTP 请求方法 method 不可为空！");
    	}
    	PreHttpCall call = new PreHttpCall();
		registeTagTask(call);
    	httpClient.preprocess(this, () -> {
    		synchronized (call) {
    			if (call.canceled) {
					removeTagTask();
        		} else {
    				if (onResponse != null || onResBody != null) {
    					tag(CopyInterceptor.TAG);
					}
					call.setCall(executeCall(prepareCall(method)));
				}
			}
    	}, skipPreproc, skipSerialPreproc);
    	return call;
    }
    
    
    class PreHttpCall implements HttpCall {

		HttpCall call;
		boolean canceled = false;
    	CountDownLatch latch = new CountDownLatch(1);

		@Override
		public synchronized boolean cancel() {
			canceled = call == null || call.cancel();
			latch.countDown();
			return canceled;
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
			return canceled;
		}

		void setCall(HttpCall call) {
			this.call = call;
			latch.countDown();
		}

		@Override
		public HttpResult getResult() {
			if (timeoutAwait(latch)) {
				cancel();
				return timeoutResult();
			}
			if (canceled || call == null) {
				return new RealHttpResult(AHttpTask.this, State.CANCELED);
			}
			return call.getResult();
		}

		@Override
		public AHttpTask getTask() {
			return AHttpTask.this;
		}

    }

    public class OkHttpCall implements HttpCall {

		final Call call;
		HttpResult result;
		CountDownLatch latch = new CountDownLatch(1);
		boolean finished = false;

		OkHttpCall(Call call) {
			this.call = call;
		}

		@Override
		public synchronized boolean cancel() {
			if (result == null || !finished) {
				call.cancel();
				return true;
			}
			return false;
		}

		@Override
		public boolean isDone() {
			return result != null && finished || call.isCanceled();
		}

		@Override
		public boolean isCanceled() {
			return call.isCanceled() || (result != null && result.getState() == State.CANCELED);
		}

		@Override
		public HttpResult getResult() {
			if (result == null) {
				if (timeoutAwait(latch)) {
					cancel();
					return timeoutResult();
				}
			}
			return result;
		}

		@Override
		public AHttpTask getTask() {
			return AHttpTask.this;
		}

		void setResult(HttpResult result) {
			this.result = result;
			latch.countDown();
		}

		public void finish() {
			this.finished = true;
		}
	}

	
    private HttpCall executeCall(Call call) {
        OkHttpCall httpCall = new OkHttpCall(call);
        call.enqueue(new Callback() {

            @Override
			@SuppressWarnings("NullableProblems")
            public void onFailure(Call call, IOException error) {
				State state = toState(error);
				HttpResult result = new RealHttpResult(AHttpTask.this, state, error);
				onCallback(httpCall, result, () -> {
					TaskExecutor executor = httpClient.executor();
					executor.executeOnComplete(AHttpTask.this, onComplete, state, completeOnIO);
					if (!httpCall.isCanceled() && !executor.executeOnException(AHttpTask.this, httpCall, onException, error, exceptionOnIO)
							&& !nothrow) {
						throw new OkHttpsException(state, "异步请求异常：" + getUrl(), error);
					}
				});
            }

			@Override
			@SuppressWarnings("NullableProblems")
            public void onResponse(Call call, Response response) {
            	TaskExecutor executor = httpClient.executor();
				HttpResult result = new RealHttpResult(AHttpTask.this, response, executor);
				onCallback(httpCall, result, () -> {
					executor.executeOnComplete(AHttpTask.this, onComplete, State.RESPONSED, completeOnIO);
					if (!httpCall.isCanceled()) {
						executor.executeOnResponse(AHttpTask.this, httpCall, complexOnResponse(httpCall), result, true);
					}
				});
            }

        });
		return httpCall;
    }

	@SuppressWarnings("all")
	private void onCallback(OkHttpCall httpCall, HttpResult result, Runnable runnable) {
		synchronized (httpCall) {
			removeTagTask();
			if (httpCall.isCanceled() || result.getState() == State.CANCELED) {
				httpCall.setResult(new RealHttpResult(AHttpTask.this, State.CANCELED));
			} else {
				httpCall.setResult(result);
			}
			runnable.run();
		}
	}

	interface ResponseCallback {
		void on(Runnable runnable, boolean onIo);
	}

	@SuppressWarnings("unchecked")
    private synchronized Consumer<HttpResult> complexOnResponse(OkHttpCall call) {
		return res -> {
			Consumer<HttpResult> onResp = onResponse;
			Consumer<HttpResult.Body> onBody = onResBody;
			Consumer<Mapper> onMapper = onResMapper;
			Consumer<Array> onArray = onResArray;
			Consumer<Object> onBean = (Consumer<Object>) onResBean;
			Consumer<Object> onList = (Consumer<Object>) onResList;
			Consumer<String> onString = onResString;

			int count = 0;
			if (onResp != null)
				count++;
			if (onBody != null)
				count++;
			if (onMapper != null)
				count++;
			if (onArray != null)
				count++;
			if (onBean != null)
				count++;
			if (onList != null)
				count++;
			if (onString != null)
				count++;
			int callbackCount = count;

			HttpResult.Body body = res.getBody();
			if (callbackCount > 1) {
				// 如果回调数量多于 1 个，则为报文体自动开启缓存
				body.cache();
			}

			ResponseCallback callback = (runnable, onIo) -> execute(new Runnable() {
				// 记录已经回调的次数
				int count = 0;
				@Override
				public void run() {
					if (!call.isCanceled()) {
						runnable.run();
					}
					if (++count >= callbackCount) {
						call.finish();
					}
				}
			}, onIo);

			if (onResp != null) {
				callback.on(() -> onResp.accept(res), responseOnIO);
			}
			if (onBody != null) {
				callback.on(() -> onBody.accept(body), resBodyOnIO);
			}
			if (onMapper != null) {
				Mapper mapper = body.toMapper();
				callback.on(() -> onMapper.accept(mapper), resMapperOnIO);
			}
			if (onArray != null) {
				Array array = body.toArray();
				callback.on(() -> onArray.accept(array), resArrayOnIO);
			}
			if (onBean != null) {
				Object bean = body.toBean(beanType);
				callback.on(() -> onBean.accept(bean), resBeanOnIO);
			}
			if (onList != null) {
				List<?> list = body.toList(listType);
				callback.on(() -> onList.accept(list), resListOnIO);
			}
			if (onString != null) {
				String string = body.toString();
				callback.on(() -> onString.accept(string), resStringOnIO);
			}
		};
	}

    private void initBeanType(Type type) {
    	if (type == null) {
    		throw new IllegalArgumentException(" bean type can not be null!");
    	}
    	if (beanType != null) {
    		throw new IllegalStateException("已经添加了 OnResBean 回调！");
    	}
    	beanType = type;
    }

}
