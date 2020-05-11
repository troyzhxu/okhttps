package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * JSON 对象
 * @author 15735
 *
 */
public interface Mapper {

	/**
	 * @return JSON 的键值对数量
	 */
	int size();
	
	/**
	 * @return 是否为空
	 */
	boolean isEmpty();
	
	/**
	 * @param key 键名
	 * @return 子 JsonObj
	 */
	Mapper getMapper(String key);
	
	/**
	 * @param key 键名
	 * @return 子 JsonArr
	 */
	Array getArray(String key);
	
	/**
	 * @param key 键名
	 * @return boolean 值
	 */
	boolean getBool(String key);

	/**
	 * @param key 键名
	 * @return int 值
	 */
	int getInt(String key);
	
	/**
	 * @param key 键名
	 * @return long 值
	 */
	long getLong(String key);
	
	/**
	 * @param key 键名
	 * @return float 值
	 */
	float getFloat(String key);
	
	/**
	 * @param key 键名
	 * @return double 值
	 */
	double getDouble(String key);
	
	/**
	 * @param key 键名
	 * @return String 值
	 */
	String getString(String key);
	
	/**
	 * @param key 键名
	 * @return BigDecimal 值
	 */
	BigDecimal getBigDecimal(String key);
	
	/**
	 * @param key 键名
	 * @return BigInteger 值
	 */
	BigInteger getBigInteger(String key);
	
	/**
	 * @param key 键名
	 * @return 是否有该键
	 */
	boolean has(String key);
	
	/**
	 * @return JSON 的键集合
	 */
	Set<String> keySet();

}
