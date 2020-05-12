package com.ejlchina.okhttps.internal;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.WebSocket.Close;
import com.ejlchina.okhttps.WebSocket.Listener;
import com.ejlchina.okhttps.WebSocket.Message;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class WebSocketTask extends HttpTask<WebSocketTask> {

	
	private Listener<HttpResult> onOpen;
	private Listener<Throwable> onException;
	private Listener<Message> onMessage;
	private Listener<Close> onClosing;
	private Listener<Close> onClosed;
	

	public WebSocketTask(HttpClient httpClient, String url) {
		super(httpClient, url);
	}

	/**
	 * 启动 WebSocket 监听
	 * @return WebSocket
	 */
	public WebSocket listen() {
		String bodyType = getBodyType();
		String msgType = bodyType.toLowerCase().contains(OkHttps.FORM) ? OkHttps.JSON : bodyType;
		WebSocketImpl socket = new WebSocketImpl(httpClient.executor, msgType);
		registeTagTask(socket);
		httpClient.preprocess(this, () -> {
			synchronized (socket) {
    			if (socket.cancelOrClosed) {
					removeTagTask();
        		} else {
					Request request = prepareRequest("GET");
					httpClient.webSocket(request, new MessageListener(socket));
				}
			}
    	}, skipPreproc, skipSerialPreproc);
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
			if (onOpen != null) {
				HttpResult result = new RealHttpResult(WebSocketTask.this, response, httpClient.executor);
				onOpen.on(this.webSocket, result);
			}
		}

		// 接收文本数据 仅当  websocket 消息中的 opcode == 1  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, String text) {
			if (onMessage != null) {
				onMessage.on(this.webSocket, new WebsocketMsg(text, httpClient.executor, charset));
			}
		}

		// 接收二进制数据 仅当  websocket 消息中的 opcode == 2  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
			if (onMessage != null) {
				onMessage.on(this.webSocket, new WebsocketMsg(bytes, httpClient.executor, charset));
			}
		}

		@Override
		public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
			if (onClosing != null) {
				onClosing.on(this.webSocket, new Close(code, reason));
			}
		}

		@Override
		public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
			if (onClosed != null) {
				onClosed.on(this.webSocket, new Close(code, reason));
			}
		}

		@Override
		public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
			if (onException != null) {
				onException.on(this.webSocket,  t);
			} else if (!nothrow) {
				throw new HttpException("WebSockt 异常", t);
			}
		}
		
	}
	
	
	static class WebSocketImpl implements WebSocket {

		private boolean cancelOrClosed;

		private okhttp3.WebSocket webSocket;

		private final List<Object> queues = new ArrayList<>();

		private TaskExecutor taskExecutor;

		private Charset charset;

		private String msgType;

		public WebSocketImpl(TaskExecutor taskExecutor, String msgType) {
			this.taskExecutor = taskExecutor;
			this.msgType = msgType;
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
			if (msg instanceof String) {
				return webSocket.send((String) msg);
			}
			if (msg instanceof ByteString) {
				return webSocket.send((ByteString) msg);
			}
			if (msg instanceof byte[]) {
				return webSocket.send(ByteString.of((byte[]) msg));
			}
			byte[] bytes = taskExecutor.doMsgConvert(msgType, (MsgConvertor c) -> c.serialize(msg, charset)).data;
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
		return this;
	}

	/**
	 * 连接异常监听
	 * @param onException 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnException(Listener<Throwable> onException) {
		this.onException = onException;
		return this;
	}

	/**
	 * 消息监听
	 * @param onMessage 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnMessage(Listener<Message> onMessage) {
		this.onMessage = onMessage;
		return this;
	}

	/**
	 * 正在关闭监听
	 * @param onClosing 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnClosing(Listener<Close> onClosing) {
		this.onClosing = onClosing;
		return this;
	}

	/**
	 * 已关闭监听
	 * @param onClosed 监听器
	 * @return WebSocketTask
	 */
	public WebSocketTask setOnClosed(Listener<Close> onClosed) {
		this.onClosed = onClosed;
		return this;
	}

}
