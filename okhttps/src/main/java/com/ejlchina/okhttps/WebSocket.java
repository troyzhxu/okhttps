package com.ejlchina.okhttps;

public interface WebSocket extends Cancelable {

	/**
	 * WebSocket 消息
	 */
	interface Message extends Toable {
		
		/**
		 * 判断是文本消息还是二进制消息
		 * @return 是否是文本消息
		 */
		boolean isText();

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
