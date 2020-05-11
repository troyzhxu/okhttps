package com.ejlchina.okhttps;

import java.io.InputStream;
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
	 * @return JsonObj
	 */
	Mapper toMapper(InputStream in, Charset charset);
	
	/**
	 * 解析 Array
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return JsonArr
	 */
	Array toArray(InputStream in, Charset charset);
	
	/**
	 * 将 Java 对象序列化为字节数组
	 * @param bean Java Bean
	 * @param charset 编码格式
	 * @return 字节数组
	 */
	byte[] serialize(Object bean, Charset charset);
	
	/**
	 * 将 Java 对象序列化为字节数组
	 * @param bean Java Bean
	 * @param dateFormat 日期类的处理格式
	 * @param charset 编码格式
	 * @return 字节数组
	 */
	byte[] serialize(Object bean, String dateFormat, Charset charset);
	
	/**
	 * 解析 Java Bean
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Java Bean
	 */
	<T> T toBean(Class<T> type, InputStream in, Charset charset);

	/**
	 * 解析为 Java List
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @param charset 编码格式
	 * @return Java List
	 */
	<T> List<T> toList(Class<T> type, InputStream in, Charset charset);

}
