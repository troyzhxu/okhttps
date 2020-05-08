package com.ejlchina.okhttps;

public interface JsonArr {

	/**
	 * @return JSON 的键值对数量
	 */
	int size();
	
	/**
	 * @return 是否为空
	 */
	boolean isEmpty();
	
	/**
	 * @param index 元素下标
	 * @return 子 JsonObj
	 */
	JsonObj getJsonOjb(int index);
	
	/**
	 * @param index 元素下标
	 * @return 子 JsonArr
	 */
	JsonArr getJsonArr(int index);
	
	/**
	 * @param index 元素下标
	 * @return Boolean 值
	 */
	Boolean getBool(int index);
	
	/**
	 * @param index 元素下标
	 * @return boolean 值
	 */
	boolean getBoolVal(int index);
	
	/**
	 * @param index 元素下标
	 * @return Integer 值
	 */
	Integer getInt(int index);

	/**
	 * @param index 元素下标
	 * @return int 值
	 */
	int getIntVal(int index);
	
	/**
	 * @param index 元素下标
	 * @return Short 值
	 */
	Short getShort(int index);
	
	/**
	 * @param index 元素下标
	 * @return short 值
	 */
	short getShortVal(int index);
	
	/**
	 * @param index 元素下标
	 * @return Float 值
	 */
	Float getFloat(int index);
	
	/**
	 * @param index 元素下标
	 * @return float 值
	 */
	float getFloatVal(int index);
	
	/**
	 * @param index 元素下标
	 * @return Double 值
	 */
	Double getDouble(int index);
	
	/**
	 * @param index 元素下标
	 * @return double 值
	 */
	double getDoubleVal(int index);
	
	/**
	 * @param index 元素下标
	 * @return String 值
	 */
	String getString(int index);
	
}
