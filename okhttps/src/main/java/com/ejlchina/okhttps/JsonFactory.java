package com.ejlchina.okhttps;

import java.util.List;


public interface JsonFactory {

	
	/**
	 * 将 json 文本解析为 JsonObj
	 * @param json 文本
	 * @return JsonObj
	 */
	JsonObj newJsonObj(String json);
	
	/**
	 * 将 json 文本解析为 JsonArr
	 * @param json 文本
	 * @return JsonArr
	 */
	JsonArr newJsonArr(String json);
	
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
	 * 将 json 文本 解析为 Java Bean
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param json 文本
	 * @return Java Bean
	 */
	<T> T jsonToBean(Class<T> type, String json);

	/**
	 * 将 json 文本 解析为 Java List
	 * @param <T> 目标泛型
	 * @param type 目标类型
	 * @param json 文本
	 * @return Java List
	 */
	<T> List<T> jsonToList(Class<T> type, String json);
	
	
}
