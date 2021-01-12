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

	/**
	 * 遍历 Array
	 * @since 2.5.1
	 * @param consumer 消费者
	 */
	default void forEach(Consumer<Integer> consumer) {
		for (int i = 0; i < size(); i++) {
			int index = i;
			consumer.accept(i, new Data() {
				@Override
				public Mapper toMapper() {
					return getMapper(index);
				}

				@Override
				public Array toArray() {
					return getArray(index);
				}

				@Override
				public boolean toBool() {
					return getBool(index);
				}

				@Override
				public int toInt() {
					return getInt(index);
				}

				@Override
				public long toLong() {
					return getLong(index);
				}

				@Override
				public float toFloat() {
					return getFloat(index);
				}

				@Override
				public double toDouble() {
					return getDouble(index);
				}

				@Override
				public String toString() {
					return getString(index);
				}

			});
		}
	}

}
