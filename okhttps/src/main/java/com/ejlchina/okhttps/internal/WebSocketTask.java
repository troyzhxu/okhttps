package com.ejlchina.okhttps.internal;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.WebSocket.Close;
import com.ejlchina.okhttps.WebSocket.Listener;
import com.ejlchina.okhttps.WebSocket.Message;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;


public class WebSocketTask extends HttpTask<WebSocketTask> {

	
	private Listener<HttpResult> onOpen;
	private Listener<Throwable> onException;
	private Listener<Message> onMessage;
	private Listener<Close> onClosing;
	private Listener<Close> onClosed;

	private boolean openOnIO;
	private boolean exceptionOnIO;
	private boolean messageOnIO;
	private boolean closingOnIO;
	private boolean closedOnIO;

	private int pingSeconds = -1;
	private int pongSeconds = -1;
	private long lastPingSecs = 0;
	private long lastPongSecs = 0;

	private boolean opened = true;

	private WebSocketImpl webSocket;


	public WebSocketTask(HttpClient httpClient, String url) {
		super(httpClient, url);
	}

	@Override
	public boolean isWebsocket() {
		return true;
	}

	/**
	 * 设置心跳间隔
	 * 覆盖 OkHttp 原有的心跳模式，主要区别如下：
	 *
	 * 1、客户端发送的任何消息都具有一次心跳作用
	 * 2、服务器发送的任何消息都具有一次心跳作用
	 * 3、若服务器超过 3 * pongSeconds 秒没有回复心跳，才判断心跳超时
	 *
	 * @param pingSeconds 客户端心跳间隔秒数（0 表示不需要心跳）
	 * @param pongSeconds 服务器心跳间隔秒数（0 表示不需要心跳）
	 * @return WebSocketTask
	 */
	public WebSocketTask heatbeat(int pingSeconds, int pongSeconds) {
		if (pingSeconds < 0 || pongSeconds < 0) {
			throw new IllegalArgumentException("pingSeconds and pongSeconds must greater equal than 0!");
		}
		this.pingSeconds = pingSeconds;
		this.pongSeconds = pongSeconds;
		return this;
	}

	/**
	 * 启动 WebSocket 监听
	 * @return WebSocket
	 */
	public WebSocket listen() {
		WebSocketImpl socket = new WebSocketImpl();
		registeTagTask(socket);
		httpClient.preprocess(this, () -> {
			synchronized (socket) {
    			if (socket.cancelOrClosed) {
					removeTagTask();
        		} else {
					Request request = prepareRequest("GET");
					MessageListener listener = new MessageListener(socket);
					if (pingSeconds > -1 || pongSeconds > -1) {
						new RealWebSocket(request, listener, new Random(), 0).connect(httpClient.okClient);
					} else {
						httpClient.webSocket(request, listener);
					}
				}
			}
    	}, skipPreproc, skipSerialPreproc);
		webSocket = socket;
		return socket;
	}
	
	
	class MessageListener extends WebSocketListener {

		WebSocketImpl webSocket;

		Charset charset;

		public MessageListener(WebSocketImpl webSocket) {
			this.webSocket = webSocket;
		}

		@Override
		public void onOpen(okhttp3.WebSocket webSocket, Response response) {
			this.charset = charset(response);
			this.webSocket.setCharset(charset);
			this.webSocket.setWebSocket(webSocket);
			TaskListener<HttpResult> listener = httpClient.executor.getResponseListener();
			HttpResult result = new RealHttpResult(WebSocketTask.this, response, httpClient.executor);
			if (listener != null) {
				if (listener.listen(WebSocketTask.this, result) && onOpen != null) {
					execute(() -> onOpen.on(this.webSocket, result), openOnIO);
				}
			} else if (onOpen != null) {
				execute(() -> onOpen.on(this.webSocket, result), openOnIO);
			}
			opened = true;
			if (pingSeconds > 0) {
				schedulePing();
			}
			if (pongSeconds > 0) {
				schedulePong();
			}
		}

		// 接收文本数据 仅当  websocket 消息中的 opcode == 1  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, String text) {
			if (onMessage != null) {
				execute(() -> onMessage.on(this.webSocket, new WebSocketMsg(text, httpClient.executor, charset)), messageOnIO);
			}
			lastPongSecs = nowSeconds();
		}

		// 接收二进制数据 仅当  websocket 消息中的 opcode == 2  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
			if (onMessage != null) {
				execute(() -> onMessage.on(this.webSocket, new WebSocketMsg(bytes, httpClient.executor, charset)), messageOnIO);
			}
			lastPongSecs = nowSeconds();
		}

		@Override
		public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
			opened = false;
			if (onClosing != null) {
				execute(() -> onClosing.on(this.webSocket, new Close(code, reason)), closingOnIO);
			}
		}

		@Override
		public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
			doOnClose(HttpResult.State.RESPONSED, code, reason);
		}

		private void doOnClose(HttpResult.State state, int code, String reason) {
			opened = false;
			TaskListener<HttpResult.State> listener = httpClient.executor.getCompleteListener();
			if (listener != null) {
				if (listener.listen(WebSocketTask.this, state) && onClosed != null) {
					execute(() -> onClosed.on(this.webSocket, toClose(state, code, reason)), closedOnIO);
				}
			} else if (onClosed != null) {
				execute(() -> onClosed.on(this.webSocket, toClose(state, code, reason)), closedOnIO);
			}
		}

		private Close toClose(HttpResult.State state, int code, String reason) {
			if (state == HttpResult.State.CANCELED) {
				return new Close(Close.CANCELED, "Canceled");
			}
			if (state == HttpResult.State.EXCEPTION) {
				return new Close(Close.CANCELED, reason);
			}
			if (state == HttpResult.State.NETWORK_ERROR) {
				return new Close(Close.NETWORK_ERROR, reason);
			}
			if (state == HttpResult.State.TIMEOUT) {
				return new Close(Close.TIMEOUT, reason);
			}
			return new Close(code, reason);
		}

		@Override
		public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
			IOException e = t instanceof IOException ? (IOException) t : new IOException(t.getMessage(), t);
			doOnClose(toState(e), 0, t.getMessage());
			TaskListener<IOException> listener = httpClient.executor.getExceptionListener();
			if (listener != null) {
				if (listener.listen(WebSocketTask.this,  e) && onException != null) {
					execute(() -> onException.on(this.webSocket,  t), exceptionOnIO);
				}
			} else if (onException != null) {
				execute(() -> onException.on(this.webSocket,  t), exceptionOnIO);
			} else if (!nothrow) {
				throw new HttpException("WebSockt 异常", t);
			}
		}
		
	}

	/**
	 * 间隔发送心跳
	 */
	private void schedulePing() {
		if (!opened) {
			return;
		}
		int delay = lastPingSecs > 0 ? (int) (pingSeconds + lastPingSecs - nowSeconds()) : pingSeconds;
		httpClient.executor.requireScheduler().schedule(() -> {
			if (!opened) {
				return;
			}
			if (nowSeconds() - lastPingSecs >= pingSeconds) {
				webSocket.send(ByteString.EMPTY);
				Platform.logInfo("PING >>>");
				lastPingSecs = nowSeconds();
			}
			schedulePing();
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * 检测服务器的心跳响应
	 */
	private void schedulePong() {
		if (!opened) {
			return;
		}
		int delay = lastPongSecs > 0 ? (int) (pongSeconds + lastPongSecs - nowSeconds()) : pongSeconds;
		httpClient.executor.requireScheduler().schedule(() -> {
			if (!opened) {
				return;
			}
			long noPongSeconds = nowSeconds() - lastPongSecs;
			if (noPongSeconds > 3 * pongSeconds) {
				SocketTimeoutException e = new SocketTimeoutException("Server didn't pong heart-beat on time. Last received at " + delay + " mills ago.");
				((RealWebSocket) webSocket.webSocket).failWebSocket(e, null);
			} else {
				schedulePong();
			}
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * @return 当前时间戳（秒）
	 */
	private long nowSeconds() {
		return System.nanoTime() / 1000_000_000;
	}

	class WebSocketImpl implements WebSocket {

		private boolean cancelOrClosed;

		private okhttp3.WebSocket webSocket;

		private final List<Object> queues = new ArrayList<>();

		private Charset charset;

		private String msgType;

		public WebSocketImpl() {
			String bodyType = getBodyType();
			this.msgType = OkHttps.FORM.equalsIgnoreCase(bodyType) ? OkHttps.JSON : bodyType;
		}

		public void setCharset(Charset charset) {
			this.charset = charset;
		}

		@Override
		public synchronized boolean cancel() {
			if (webSocket != null) {
				webSocket.cancel();
			}
			cancelOrClosed = true;
			return true;
		}

		@Override
		public synchronized boolean close(int code, String reason) {
			if (webSocket != null) {
				webSocket.close(code, reason);
			}
			cancelOrClosed = true;
			return true;
		}

		@Override
		public void msgType(String type) {
			if (type == null || type.equalsIgnoreCase(OkHttps.FORM)) {
				throw new IllegalArgumentException("msgType 不可为空 或 form");
			}
			this.msgType = type;
		}

		@Override
		public long queueSize() {
			if (webSocket != null) {
				return webSocket.queueSize();
			}
			return queues.size();
		}

		@Override
		public boolean send(Object msg) {
			if (msg == null) {
				return false;
			}
			synchronized (queues) {
				if (webSocket != null) {
					return send(webSocket, msg);
				} else {
					queues.add(msg);
				}
			}
			return true;
		}
		
		void setWebSocket(okhttp3.WebSocket webSocket) {
			synchronized (queues) {
				for (Object msg: queues) {
					send(webSocket, msg);
				}
				this.webSocket = webSocket;
				queues.clear();
			}
		}
		
		boolean send(okhttp3.WebSocket webSocket, Object msg) {
			if (msg == null) {
				return false;
			}
			lastPingSecs = nowSeconds();
			if (msg instanceof String) {
				return webSocket.send((String) msg);
			}
			if (msg instanceof ByteString) {
				return webSocket.send((ByteString) msg);
			}
			if (msg instanceof byte[]) {
				return webSocket.send(ByteString.of((byte[]) msg));
			}
			byte[] bytes = httpClient.executor.doMsgConvert(msgType, (MsgConvertor c) -> c.serialize(msg, charset)).data;
			return webSocket.send(new String(bytes, charset));
		}
		
	}

	/**
	 * 连接打开监听
	 * @param onOpen 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnOpen(Listener<HttpResult> onOpen) {
		this.onOpen = onOpen;
		openOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 连接异常监听
	 * @param onException 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnException(Listener<Throwable> onException) {
		this.onException = onException;
		exceptionOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 消息监听
	 * @param onMessage 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnMessage(Listener<Message> onMessage) {
		this.onMessage = onMessage;
		messageOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 正在关闭监听
	 * @param onClosing 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnClosing(Listener<Close> onClosing) {
		this.onClosing = onClosing;
		closingOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 已关闭监听（当连接被取消或发生异常时，也会走该回调）
	 * @param onClosed 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnClosed(Listener<Close> onClosed) {
		this.onClosed = onClosed;
		closedOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

}
