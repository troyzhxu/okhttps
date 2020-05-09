package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;

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
	 * @return boolean 值
	 */
	boolean getBool(int index);

	/**
	 * @param index 元素下标
	 * @return int 值
	 */
	int getInt(int index);
	
	/**
	 * @param index 元素下标
	 * @return long 值
	 */
	long getLong(int index);
	
	/**
	 * @param index 元素下标
	 * @return float 值
	 */
	float getFloat(int index);
	
	/**
	 * @param index 元素下标
	 * @return double 值
	 */
	double getDouble(int index);
	
	/**
	 * @param index 元素下标
	 * @return String 值
	 */
	String getString(int index);
	
	/**
	 * @param index 元素下标
	 * @return BigDecimal 值
	 */
	BigDecimal getBigDecimal(int index);
	
	/**
	 * @param index 元素下标
	 * @return BigInteger 值
	 */
	BigInteger getBigInteger(int index);

}
