package com.ejlchina.okhttps;

import okio.ByteString;

public interface WebSocket extends Cancelable {


	public interface Listener<T> {
		
		void on(WebSocket ws, T data);
		
	}
	
	
	/**
	 * 同 {@link okhttp3.WebSocket#queueSize()}
	 */
	long queueSize();

	/**
	 * 同 {@link okhttp3.WebSocket#send(String)}
	 */
	boolean send(String text);

	/**
	 * 同 {@link okhttp3.WebSocket#send(ByteString)}
	 */
	boolean send(ByteString bytes);

	/**
	 * 以JSON文本格式发送对象消息
	 * @param bean 待发送的对象
	 * @return 如果连接已断开 返回 false
	 */
	public boolean send(Object bean);

	/**
	 * 发送字节流
	 * @param data 待发送的数据
	 * @return 如果连接已断开 返回 false
	 */
	public boolean send(byte[] data);
	
	/**
	 * 同 {@link okhttp3.WebSocket#close(int, String)}
	 */
	boolean close(int code, String reason);
	
}
