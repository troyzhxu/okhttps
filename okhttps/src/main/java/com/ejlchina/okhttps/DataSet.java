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

}
