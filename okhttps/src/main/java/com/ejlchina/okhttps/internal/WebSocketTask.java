package com.ejlchina.okhttps.internal;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.HttpTask;
import com.ejlchina.okhttps.WebSocket;
import com.ejlchina.okhttps.WebSocket.Listener;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class WebSocketTask extends HttpTask<WebSocketTask> {

	
	private Listener<HttpResult> onOnen;
	private Listener<Throwable> onException;
	

	public WebSocketTask(HttpClient httpClient, String url) {
		super(httpClient, url);
	}

	
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
					socket.setWebSocket(httpClient.webSocket(request, listener));
				}
			}
    	}, noPreprocess, noSerialPreprocess);
		return socket;
	}
	
	
	class MessageListener extends WebSocketListener {

		WebSocket webSocket;

		public MessageListener(WebSocket webSocket) {
			this.webSocket = webSocket;
		}

		@Override
		public void onOpen(okhttp3.WebSocket webSocket, Response response) {
			if (onOnen != null) {
				HttpResult result = new RealHttpResult(WebSocketTask.this, response, httpClient.executor);
				onOnen.on(this.webSocket, result);
			}
		}

		@Override
		public void onMessage(okhttp3.WebSocket webSocket, String text) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
			// TODO Auto-generated method stub
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
		
		List<Object> queues = new ArrayList<>();
		
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
			return 0;
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
			if (webSocket != null) {
				return send(webSocket, bean);
			}
			queueMsgToSend(bean);
			return true;
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
				if (queues != null) {
					queues.add(msg);
				} else if (webSocket != null) {
					send(webSocket, msg);
				} else {
					throw new IllegalStateException();
				}
			}
		}
		
		void setWebSocket(okhttp3.WebSocket webSocket) {
			synchronized (queues) {
				for (Object msg: queues) {
					send(webSocket, msg);
				}
				this.webSocket = webSocket;
				queues = null;
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
			return webSocket.send(JSON.toJSONString(msg));
		}
		
	}

}
