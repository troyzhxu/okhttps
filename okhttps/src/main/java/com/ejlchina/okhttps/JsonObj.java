package com.ejlchina.okhttps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

/**
 * JSON 对象
 * @author 15735
 *
 */
public interface JsonObj {

	/**
	 * @return JSON 的键值对数量
	 */
	public int size();
	
	/**
	 * @return 是否为空
	 */
	boolean isEmpty();
	
	/**
	 * @param key 键名
	 * @return 子 JsonObj
	 */
	JsonObj getJsonOjb(String key);
	
	/**
	 * @param key 键名
	 * @return 子 JsonArr
	 */
	JsonArr getJsonArr(String key);
	
	/**
	 * @param key 键名
	 * @return Boolean 值
	 */
	Boolean getBool(String key);
	
	/**
	 * @param key 键名
	 * @return boolean 值
	 */
	boolean getBoolVal(String key);
	
	/**
	 * @param key 键名
	 * @return Integer 值
	 */
	Integer getInt(String key);

	/**
	 * @param key 键名
	 * @return int 值
	 */
	int getIntVal(String key);
	
	/**
	 * @param key 键名
	 * @return Short 值
	 */
	Short getShort(String key);
	
	/**
	 * @param key 键名
	 * @return short 值
	 */
	short getShortVal(String key);
	
	/**
	 * @param key 键名
	 * @return Float 值
	 */
	Float getFloat(String key);
	
	/**
	 * @param key 键名
	 * @return float 值
	 */
	float getFloatVal(String key);
	
	/**
	 * @param key 键名
	 * @return Double 值
	 */
	Double getDouble(String key);
	
	/**
	 * @param key 键名
	 * @return double 值
	 */
	double getDoubleVal(String key);
	
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
	public Set<String> keySet();

}
