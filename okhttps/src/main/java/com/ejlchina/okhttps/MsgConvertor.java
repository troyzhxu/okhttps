package com.ejlchina.okhttps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 消息转换器接口
 */
public interface MsgConvertor {

	/**
	 * 消息的媒体类型
	 * @return 媒体类型
	 */
	String mediaType();

	/**
	 * 解析 Mapper
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Mapper
	 */
	Mapper toMapper(InputStream in, Charset charset);
	
	/**
	 * 解析 Array
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Array
	 */
	Array toArray(InputStream in, Charset charset);
	
	/**
	 * 将 Java 对象序列化为字节数组
	 * @param object Java 对象
	 * @param charset 编码格式
	 * @return 字节数组
	 */
	byte[] serialize(Object object, Charset charset);
	
	/**
	 * 将 Java 对象序列化为字节数组
	 * @param object Java 对象
	 * @param dateFormat 日期类的处理格式
	 * @param charset 编码格式
	 * @return 字节数组
	 */
	byte[] serialize(Object object, String dateFormat, Charset charset);
	
	/**
	 * 解析 Java Bean
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Java Bean
	 */
	<T> T toBean(Type type, InputStream in, Charset charset);

	/**
	 * 解析为 Java List
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Java List
	 */
	<T> List<T> toList(Class<T> type, InputStream in, Charset charset);

	/**
	 * 表单转换器，可用于自动系列化表单参数
	 */
	class FormConvertor implements MsgConvertor {

		private MsgConvertor convertor;

		public FormConvertor(MsgConvertor convertor) {
			this.convertor = convertor;
		}

		@Override
		public String mediaType() {
			return "application/x-www-form-urlencoded";
		}

		@Override
		public Mapper toMapper(InputStream in, Charset charset) {
			return convertor.toMapper(in, charset);
		}

		@Override
		public Array toArray(InputStream in, Charset charset) {
			return convertor.toArray(in, charset);
		}

		@Override
		public byte[] serialize(Object object, Charset charset) {
			return serialize(object, null, charset);
		}

		@Override
		public byte[] serialize(Object object, String dateFormat, Charset charset) {
			byte[] data = convertor.serialize(object, dateFormat, charset);
			Mapper mapper = convertor.toMapper(new ByteArrayInputStream(data), charset);
			StringBuilder sb = new StringBuilder();
			for (String key: mapper.keySet()) {
				sb.append(key).append('=').append(mapper.getString(key)).append('&');
			}
			if (sb.length() > 1) {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString().getBytes(charset);
		}

		@Override
		public <T> T toBean(Type type, InputStream in, Charset charset) {
			return convertor.toBean(type, in, charset);
		}

		@Override
		public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
			return convertor.toList(type, in, charset);
		}

	}

}
