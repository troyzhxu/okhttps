package com.ejlchina.okhttps.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.WebSocket;
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
		WebSocketImpl socket = new WebSocketImpl(httpClient.executor, httpClient.charset);
		registeTagTask(socket);
		httpClient.preprocess(this, () -> {
			synchronized (socket) {
    			if (socket.cancelOrClosed) {
					removeTagTask();
        		} else {
					Request request = prepareRequest("GET");
					MessageListener listener = new MessageListener(socket);
					socket.setWebSocket(httpClient.webSocket(request, listener));
				}
			}
    	}, skipPreproc, skipSerialPreproc);
		return socket;
	}
	
	
	class MessageListener extends WebSocketListener {

		WebSocket webSocket;

		public MessageListener(WebSocket webSocket) {
			this.webSocket = webSocket;
		}

		@Override
		public void onOpen(okhttp3.WebSocket webSocket, Response response) {
			if (onOpen != null) {
				HttpResult result = new RealHttpResult(WebSocketTask.this, response, httpClient.executor);
				onOpen.on(this.webSocket, result);
			}
		}

		// 接收文本数据 仅当  websocket 消息中的 opcode == 1  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, String text) {
			if (onMessage != null) {
				onMessage.on(this.webSocket, new MessageBody(text, httpClient.executor));
			}
		}

		// 接收二进制数据 仅当  websocket 消息中的 opcode == 2  时
		@Override
		public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
			if (onMessage != null) {
				onMessage.on(this.webSocket, new MessageBody(bytes, httpClient.executor));
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

		boolean cancelOrClosed;

		okhttp3.WebSocket webSocket;
		
		final List<Object> queues = new ArrayList<>();
		
		TaskExecutor taskExecutor;

		Charset charset;

		public WebSocketImpl(TaskExecutor taskExecutor, Charset charset) {
			this.taskExecutor = taskExecutor;
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
		public long queueSize() {
			if (webSocket != null) {
				return webSocket.queueSize();
			}
			return queues.size();
		}

		@Override
		public boolean send(String text) {
			if (webSocket != null) {
				return webSocket.send(text);
			}
			queueMsgToSend(text);
			return true;
		}

		@Override
		public boolean send(ByteString bytes) {
			if (webSocket != null) {
				return webSocket.send(bytes);
			}
			queueMsgToSend(bytes);
			return true;
		}

		@Override
		public boolean send(Object bean) {
			return send(taskExecutor.convertor().serialize(bean, charset));
		}

		@Override
		public boolean send(Object bean, String dateFormat) {
			return send(taskExecutor.convertor().serialize(bean, dateFormat, charset));
		}
		
		@Override
		public boolean send(byte[] data) {
			if (webSocket != null) {
				return send(webSocket, data);
			}
			queueMsgToSend(data);
			return true;
		}
		
		
		void queueMsgToSend(Object msg) {
			if (msg == null) {
				return;
			}
			synchronized (queues) {
				if (webSocket != null) {
					send(webSocket, msg);
				} else {
					queues.add(msg);
				}
			}
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
			byte[] bytes = taskExecutor.convertor().serialize(msg, charset);
			return webSocket.send(new String(bytes, StandardCharsets.UTF_8));
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
