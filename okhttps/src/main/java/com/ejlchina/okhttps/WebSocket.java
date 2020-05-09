package com.ejlchina.okhttps;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import okio.ByteString;

public interface WebSocket extends Cancelable {


	public interface Message {
		
		/**
		 * 判断是文本消息还是二进制消息
		 * @return 是否是文本消息
		 */
		boolean isText();
		
		/**
		 * @return 消息体转字节流
		 */
		InputStream toByteStream();
		
		/**
		 * @return 消息体转字节数组
		 */
		byte[] toBytes();
		
		/**
		 * @return ByteString
		 */
		ByteString toByteString();
		
		/**
		 * @return 消息体转字符流
		 */
		Reader toCharStream();

		/**
		 * @return 消息体转字符串
		 */
		String toString();

		/**
		 * @return 消息体转Json对象
		 */
		JsonObj toJsonObj();

		/**
		 * @return 消息体转Json数组
		 */
		JsonArr toJsonArr();

		/**
		 * @param <T> 目标泛型
		 * @param type 目标类型
		 * @return 报文体Json文本转JavaBean
		 */
		<T> T toBean(Class<T> type);

		/**
		 * @param <T> 目标泛型
		 * @param type 目标类型
		 * @return 报文体Json文本转JavaBean列表
		 */
		<T> List<T> toList(Class<T> type);
		
	}
	

	public static class Close {
		
		private int code;
		private String reason;
		
		public Close(int code, String reason) {
			this.code = code;
			this.reason = reason;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		@Override
		public String toString() {
			return "Close [code=" + code + ", reason=" + reason + "]";
		}
	}
	

	public interface Listener<T> {
		
		void on(WebSocket ws, T data);
		
	}
	
	/**
	 * 若连接已打开，则：
	 * 同 {@link okhttp3.WebSocket#queueSize()}，返回排序消息的字节数
	 * 否则：
	 * 返回排队消息的数量
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
	 * 以JSON文本格式发送对象消息
	 * @param bean 待发送的对象
	 * @param dateFormat 日期类型字段的处理格式
	 * @return 如果连接已断开 返回 false
	 */
	public boolean send(Object bean, String dateFormat);
	
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
