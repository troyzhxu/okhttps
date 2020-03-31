package com.ejlchina.http;

/**
 * 数据回调接口
 * Created by 15735 on 2017/1/3.
 */
public interface OnCallback<T> {

    void on(T data);

}
