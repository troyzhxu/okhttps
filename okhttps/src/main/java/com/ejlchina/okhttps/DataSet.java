package com.ejlchina.okhttps;

/**
 * 只读数据集
 * @since 2.5.0
 */
public interface DataSet {

    /**
     * @return 集合内直接子元素的数量
     */
    int size();

    /**
     * @return 数据集是否为空
     */
    boolean isEmpty();

    /**
     * 只读数据
     * @since 2.5.1
     */
    interface Data {

        Mapper toMapper();

        Array toArray();

        boolean toBool();

        int toInt();

        long toLong();

        float toFloat();

        double toDouble();

        String toString();

    }

    /**
     * 消费者，用于 DataSet 的遍历
     * @since 2.5.1
     * @see Array#forEach(Consumer)
     * @see Mapper#forEach(Consumer)
     * @param <T> 下标或键值的类型
     */
    interface Consumer<T> {

        /**
         * 消费方法
         * @param indexKey Array 的下标 或 Mapper 的键值
         * @param data 数据元素
         */
        void accept(T indexKey, Data data);

    }

}
