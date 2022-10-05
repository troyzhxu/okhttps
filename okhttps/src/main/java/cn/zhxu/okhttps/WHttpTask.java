package cn.zhxu.okhttps;

import cn.zhxu.okhttps.WebSocket.Close;
import cn.zhxu.okhttps.WebSocket.Listener;
import cn.zhxu.okhttps.WebSocket.Message;
import cn.zhxu.okhttps.internal.AbstractHttpClient;
import cn.zhxu.okhttps.internal.RealHttpResult;
import cn.zhxu.okhttps.internal.WebSocketMsg;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class WHttpTask extends HttpTask<WHttpTask> {

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

	// 心跳数据提供者
	private Supplier<String> pingSupplier;

	private WebSocketImpl webSocket;

	// Ping 的间隔是否灵活可变
	private boolean flexiblePing = true;

	// 最大关闭时长，即：执行了 OnClosing 回调后，最晚过多少久就会执行 OnClosed 回调
	private int maxClosingSecs = 10;


	public WHttpTask(AbstractHttpClient httpClient, String url) {
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
	 * 4、可指定心跳的具体内容（默认为空）
	 *
	 * 由于 OkHttp 底层并未暴露 websocket 协议里 opcode 的接口，所以该心跳的 opcode 始终是 2，并不是 websocket 协议里定义的 9
	 * 所以如果服务器要求客户端心跳的 opcode 必须是 9 的话，请使用 OkHttp 的原生心跳：
	 * [http://okhttps.ejlchina.com/v2/websocket.html#%E5%85%A8%E5%B1%80%E5%BF%83%E8%B7%B3%E9%85%8D%E7%BD%AE]
	 *
	 * 另若需要 可使用 {@link #pingSupplier(Supplier)} 方法指定心跳发送的具体内容
	 *
	 * @since v2.3.0
	 * @param pingSeconds 客户端心跳间隔秒数（0 表示不需要心跳）
	 * @param pongSeconds 服务器心跳间隔秒数（0 表示不需要心跳）
	 * @return WHttpTask
	 */
	public WHttpTask heatbeat(int pingSeconds, int pongSeconds) {
		if (pingSeconds < 0 || pongSeconds < 0) {
			throw new IllegalArgumentException("pingSeconds and pongSeconds must greater equal than 0!");
		}
		this.pingSeconds = pingSeconds;
		this.pongSeconds = pongSeconds;
		return this;
	}

	/**
	 * 用于兼容某些强制客户端必须以固定的时间间隔发送心跳的服务器
	 * @since v2.5.0
	 * @param flexiblePing Ping 的间隔是否灵活可变（默认为 true, 为 false 时客户端 Ping 的间隔固定，普通的消息不做为 Ping）
	 * @return WHttpTask
	 */
	public WHttpTask flexiblePing(boolean flexiblePing) {
		this.flexiblePing = flexiblePing;
		return this;
	}

	/**
	 * @param pingSupplier 心跳数据提供者
	 * @return WHttpTask
	 */
	public WHttpTask pingSupplier(Supplier<String> pingSupplier) {
		this.pingSupplier = pingSupplier;
		return this;
	}

	public Supplier<String> pingSupplier() {
		return pingSupplier;
	}
	
	/**
	 * 启动 WebSocket 监听
	 * @return WebSocket
	 */
	public synchronized WebSocket listen() {
		if (webSocket != null) {
			// 如果连接已建立，直接返回
			return webSocket;
		}
		WebSocketImpl socket = new WebSocketImpl();
		registeTagTask(socket);
		httpClient.preprocess(this, () -> {
			synchronized (socket) {
    			if (socket.cancelOrClosed) {
					removeTagTask();
        		} else {
					Request request = prepareRequest(HTTP.GET);
					MessageListener listener = new MessageListener(socket);
					httpClient.webSocket(request, listener);
				}
			}
    	}, skipPreproc, skipSerialPreproc);
		webSocket = socket;
		return socket;
	}

	/**
	 * @since v3.1.0
	 * @param code 状态码
	 * @param reason 原因
	 * @return true: 被关闭, false: 当前尚未建立连接
	 */
	public boolean close(int code, String reason) {
		WebSocket ws = webSocket;
		if (ws != null) {
			ws.close(code, reason);
			webSocket = null;
			return true;
		}
		return false;
	}

	@SuppressWarnings("NullableProblems")
	class MessageListener extends WebSocketListener {

		final WebSocketImpl webSocket;

		Charset charset;

		public MessageListener(WebSocketImpl webSocket) {
			this.webSocket = webSocket;
		}

		@Override
		public void onOpen(okhttp3.WebSocket webSocket, Response response) {
			this.charset = charset(response);
			this.webSocket.setCharset(charset);
			this.webSocket.setWebSocket(webSocket);
			this.webSocket.setStatus(WebSocket.STATUS_CONNECTED);
			TaskListener<HttpResult> listener = httpClient.executor().getResponseListener();
			HttpResult result = new RealHttpResult(WHttpTask.this, response, httpClient.executor());
			Listener<HttpResult> openListener = onOpen;
			if (listener != null) {
				if (listener.listen(WHttpTask.this, result) && openListener != null) {
					execute(() -> openListener.on(this.webSocket, result), openOnIO);
				}
			} else if (openListener != null) {
				execute(() -> openListener.on(this.webSocket, result), openOnIO);
			}
			if (pingSeconds > 0) {
				lastPingSecs = nowSeconds();
				schedulePing();
			}
			if (pongSeconds > 0) {
				lastPongSecs = nowSeconds();
				schedulePong();
			}
		}

		// 接收文本数据 仅当  websocket 消息中的 opcode == 1  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, String text) {
			Listener<Message> listener = onMessage;
			if (listener != null) {
				execute(() -> listener.on(this.webSocket, new WebSocketMsg(text, httpClient.executor(), charset)), messageOnIO);
			}
			if (pongSeconds > 0) {
				lastPongSecs = nowSeconds();
			}
		}

		// 接收二进制数据 仅当  websocket 消息中的 opcode == 2  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
			Listener<Message> listener = onMessage;
			if (listener != null) {
				execute(() -> listener.on(this.webSocket, new WebSocketMsg(bytes, httpClient.executor(), charset)), messageOnIO);
			}
			if (pongSeconds > 0) {
				lastPongSecs = nowSeconds();
			}
		}

		@Override
		public void onClosing(okhttp3.WebSocket ws, int code, String reason) {
			this.webSocket.setStatus(WebSocket.STATUS_DISCONNECTED);
			Listener<Close> listener = onClosing;
			if (listener != null) {
				execute(() -> listener.on(this.webSocket, new Close(code, reason)), closingOnIO);
			}
			httpClient.executor().requireScheduler().schedule(() -> {
				doOnClose(HttpResult.State.RESPONSED, code, reason);
			}, maxClosingSecs, TimeUnit.SECONDS);
		}

		@Override
		public void onClosed(okhttp3.WebSocket ws, int code, String reason) {
			doOnClose(HttpResult.State.RESPONSED, code, reason);
		}

		private void doOnClose(HttpResult.State state, int code, String reason) {
			synchronized(WHttpTask.this) {
				if (WHttpTask.this.webSocket == null || this.webSocket != WHttpTask.this.webSocket) {
					return;		// 回调已经执行过
				}
				WHttpTask.this.webSocket = null;
				Close close = updateStatus(state, code, reason);
				TaskListener<HttpResult.State> listener = httpClient.executor().getCompleteListener();
				Listener<Close> closeListener = onClosed;
				if (listener != null) {
					if (listener.listen(WHttpTask.this, state) && closeListener != null) {
						execute(() -> closeListener.on(this.webSocket, close), closedOnIO);
					}
				} else if (closeListener != null) {
					execute(() -> closeListener.on(this.webSocket, close), closedOnIO);
				}
			}
		}

		private Close updateStatus(HttpResult.State state, int code, String reason) {
			if (state == HttpResult.State.CANCELED) {
				webSocket.setStatus(WebSocket.STATUS_CANCELED);
				return new Close(Close.CANCELED, "Canceled");
			}
			if (state == HttpResult.State.EXCEPTION) {
				webSocket.setStatus(WebSocket.STATUS_EXCEPTION);
				return new Close(Close.CANCELED, reason);
			}
			if (state == HttpResult.State.NETWORK_ERROR) {
				webSocket.setStatus(WebSocket.STATUS_NETWORK_ERROR);
				return new Close(Close.NETWORK_ERROR, reason);
			}
			if (state == HttpResult.State.TIMEOUT) {
				webSocket.setStatus(WebSocket.STATUS_TIMEOUT);
				return new Close(Close.TIMEOUT, reason);
			}
			webSocket.setStatus(WebSocket.STATUS_DISCONNECTED);
			return new Close(code, reason);
		}

		@Override
		public void onFailure(okhttp3.WebSocket ws, Throwable t, Response response) {
			IOException e = t instanceof IOException ? (IOException) t : new IOException(t.getMessage(), t);
			doOnClose(toState(e), 0, t.getMessage());
			TaskListener<IOException> listener = httpClient.executor().getExceptionListener();
			Listener<Throwable> exceptionListener = onException;
			if (listener != null) {
				if (listener.listen(WHttpTask.this,  e) && exceptionListener != null) {
					execute(() -> exceptionListener.on(this.webSocket,  t), exceptionOnIO);
				}
			} else if (exceptionListener != null) {
				execute(() -> exceptionListener.on(this.webSocket,  t), exceptionOnIO);
			} else if (!nothrow) {
				throw new OkHttpsException("WebSockt 连接异常: " + getUrl(), t);
			}
		}
		
	}

	/**
	 * @return 连接是否已建立
	 */
	public boolean isConnected() {
		WebSocketImpl ws = webSocket;
		return ws != null && ws.status == WebSocket.STATUS_CONNECTED;
	}

	/**
	 * 间隔发送心跳
	 */
	private void schedulePing() {
		if (!isConnected()) {
			return;
		}
		int delay = (int) (pingSeconds + lastPingSecs - nowSeconds());
		httpClient.executor().requireScheduler().schedule(() -> {
			if (!isConnected()) {
				return;
			}
			WebSocket ws = webSocket;
			if (nowSeconds() - lastPingSecs >= pingSeconds && ws != null) {
				ws.send(pingSupplier != null ? pingSupplier.get() : ByteString.EMPTY);
				lastPingSecs = nowSeconds();
			}
			schedulePing();
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * 检测服务器的心跳响应
	 */
	private void schedulePong() {
		if (!isConnected()) {
			return;
		}
		int delay = (int) (pongSeconds + lastPongSecs - nowSeconds());
		httpClient.executor().requireScheduler().schedule(() -> {
			if (!isConnected()) {
				return;
			}
			long noPongSeconds = nowSeconds() - lastPongSecs;
			if (noPongSeconds > 3L * pongSeconds) {
				WebSocketImpl ws = webSocket;
				if (ws != null) {
					Exception e = new SocketTimeoutException("Server didn't pong heart-beat on time. Last received at " + noPongSeconds + " seconds ago.");
					((RealWebSocket) ws.webSocket).failWebSocket(e, null);
				}
			} else {
				schedulePong();
			}
		}, delay, TimeUnit.SECONDS);
	}

	/**
	 * @return 当前时间戳（秒）
	 */
	private long nowSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	class WebSocketImpl implements WebSocket {

		private boolean cancelOrClosed;

		private okhttp3.WebSocket webSocket;

		private final List<Object> queues = new ArrayList<>();

		private Charset charset;

		private String msgType;

		private int status = STATUS_CONNECTING;		// 当前的连接状态

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
		public synchronized void close(int code, String reason) {
			if (webSocket != null) {
				webSocket.close(code, reason);
			}
			cancelOrClosed = true;
		}

		@Override
		public void msgType(String type) {
			if (type == null || type.equalsIgnoreCase(OkHttps.FORM)) {
				throw new IllegalArgumentException("msgType 不可为空 或 form");
			}
			this.msgType = type.toLowerCase();
		}

		@Override
		public int status() {
			return status;
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

		public void setStatus(int status) {
			this.status = status;
		}

		boolean send(okhttp3.WebSocket webSocket, Object msg) {
			if (msg == null) {
				return false;
			}
			if (pingSeconds > 0 && flexiblePing) {
				lastPingSecs = nowSeconds();
			}
			if (msg instanceof String) {
				return webSocket.send((String) msg);
			}
			if (msg instanceof ByteString) {
				return webSocket.send((ByteString) msg);
			}
			if (msg instanceof byte[]) {
				return webSocket.send(ByteString.of((byte[]) msg));
			}
			byte[] bytes = httpClient.executor().doMsgConvert(msgType, c -> c.serialize(msg, charset)).data;
			return webSocket.send(new String(bytes, charset));
		}
		
	}

	/**
	 * 连接打开监听
	 * @param onOpen 监听器
	 * @return WHttpTask
	 */
	public WHttpTask setOnOpen(Listener<HttpResult> onOpen) {
		this.onOpen = onOpen;
		openOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 连接异常监听
	 * @param onException 监听器
	 * @return WHttpTask
	 */
	public WHttpTask setOnException(Listener<Throwable> onException) {
		this.onException = onException;
		exceptionOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 消息监听
	 * @param onMessage 监听器
	 * @return WHttpTask
	 */
	public WHttpTask setOnMessage(Listener<Message> onMessage) {
		this.onMessage = onMessage;
		messageOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 正在关闭监听
	 * @param onClosing 监听器
	 * @return WHttpTask
	 */
	public WHttpTask setOnClosing(Listener<Close> onClosing) {
		this.onClosing = onClosing;
		closingOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 已关闭监听（当连接被取消或发生异常时，也会走该回调）
	 * @param onClosed 监听器
	 * @return WHttpTask
	 */
	public WHttpTask setOnClosed(Listener<Close> onClosed) {
		this.onClosed = onClosed;
		closedOnIO = nextOnIO;
		nextOnIO = false;
		return this;
	}

	/**
	 * 设置在 OnClosing 回调执行完毕后，OnClosed 回调执行的最晚延迟时间
	 * @param maxClosingSecs 最大 Closing 时长（单位：秒，默认：10秒）
	 * @return WHttpTask
	 */
	public WHttpTask setMaxClosingSecs(int maxClosingSecs) {
		this.maxClosingSecs = maxClosingSecs;
		return this;
	}

	public int pingSeconds() {
		return pingSeconds;
	}

	public int pongSeconds() {
		return pongSeconds;
	}

}
