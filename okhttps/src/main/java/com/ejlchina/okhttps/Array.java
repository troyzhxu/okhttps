package com.ejlchina.okhttps;

/**
 * 列表结构的只读数据集
 *
 * @since 2.0.0
 * Array 接口 类似于 JsonArray
 * 但为什么不取名为 json 呢，因它不止是 json，它还可以是 xml、yml、protobuf 等任何一种格式的数据
 */
public interface Array extends DataSet {

	/**
	 * @param index 元素下标
	 * @return 子 JsonObj
	 */
	Mapper getMapper(int index);
	
	/**
	 * @param index 元素下标
	 * @return 子 JsonArr
	 */
	Array getArray(int index);
	
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

}
