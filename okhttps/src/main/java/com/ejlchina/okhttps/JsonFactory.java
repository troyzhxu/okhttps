package com.ejlchina.okhttps;

import java.io.InputStream;
import java.util.List;


public interface JsonFactory {

	
	/**
	 * 解析 JsonObj
	 * @param in 输入流
	 * @return JsonObj
	 */
	JsonObj newJsonObj(InputStream in);
	
	/**
	 * 解析 JsonArr
	 * @param in 输入流
	 * @return JsonArr
	 */
	JsonArr newJsonArr(InputStream in);
	
	/**
	 * 将 Java 对象序列化为 Json 字符串
	 * @param bean Java Bean
	 * @return Json 字符串
	 */
	String toJsonStr(Object bean);
	
	/**
	 * 将 Java 对象序列化为 Json 字符串
	 * @param bean Java Bean
	 * @param dateFormat 日期类的处理格式
	 * @return Json 字符串
	 */
	String toJsonStr(Object bean, String dateFormat);
	
	/**
	 * 解析 Java Bean
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in 输入流
	 * @return Java Bean
	 */
	<T> T jsonToBean(Class<T> type, InputStream in);

	/**
	 * 解析为 Java List
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param in 输入流
	 * @return Java List
	 */
	<T> List<T> jsonToList(Class<T> type, InputStream in);
	
	
}
