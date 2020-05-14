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

		public static int CANCELED = 0;
		public static int EXCEPTION = -1;

		private int code;
		private String reason;
		
		public Close(int code, String reason) {
			this.code = code;
			this.reason = reason;
		}

		/**
		 * @return 关闭状态码
		 */
		public int getCode() {
			return code;
		}

		/**
		 * @return 关闭原因
		 */
		public String getReason() {
			return reason;
		}

		/**
		 * @return 是否因 WebSocket 连接被取消而关闭
		 */
		public boolean isCanceled() {
			return code == CANCELED;
		}

		/**
		 * @return 是否因 WebSocket 连接发生异常而关闭
		 */
		public boolean isException() {
			return code == EXCEPTION;
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
