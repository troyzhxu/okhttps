package com.ejlchina.okhttps;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import okio.ByteString;

public interface WebSocket extends Cancelable {


	interface Message {
		
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
		Mapper toMapper();

		/**
		 * @return 消息体转Json数组
		 */
		Array toArray();

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
	

	class Close {
		
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
	

	interface Listener<T> {
		
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
	 * @param object 待发送的对象，可以是 String | ByteString | byte[] | Java Bean
	 * @return 如果连接已断开 返回 false
	 */
	boolean send(Object object);
	
	/**
	 * 同 {@link okhttp3.WebSocket#close(int, String)}
	 */
	boolean close(int code, String reason);

	/**
	 * 设置消息类型
	 * @param type 消息类型，如 json、xml、protobuf 等
	 */
	void msgType(String type);

}
