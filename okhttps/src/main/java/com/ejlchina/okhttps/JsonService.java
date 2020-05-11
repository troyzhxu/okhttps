package com.ejlchina.okhttps;

import java.io.InputStream;
import java.util.List;


public interface JsonService {

	/**
	 * 解析 Mapper
	 * @param in JSON 输入流
	 * @return JsonObj
	 */
	Mapper toMapper(InputStream in);
	
	/**
	 * 解析 Array
	 * @param in JSON 输入流
	 * @return JsonArr
	 */
	Array toArray(InputStream in);
	
	/**
	 * 将 Java 对象序列化为字符串
	 * @param bean Java Bean
	 * @return 字符串
	 */
	String serialize(Object bean);
	
	/**
	 * 将 Java 对象序列化为字符串
	 * @param bean Java Bean
	 * @param dateFormat 日期类的处理格式
	 * @return 字符串
	 */
	String serialize(Object bean, String dateFormat);
	
	/**
	 * 解析 Java Bean
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @return Java Bean
	 */
	<T> T toBean(Class<T> type, InputStream in);

	/**
	 * 解析为 Java List
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in JSON 输入流
	 * @return Java List
	 */
	<T> List<T> toList(Class<T> type, InputStream in);
	
	
}
