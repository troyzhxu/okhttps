package com.ejlchina.okhttps;

import okio.ByteString;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public interface Toable {

    /**
     * @return 消息体转字节流
     */
    InputStream toByteStream();

    /**
     * @return 消息体转字节数组
     */
    byte[] toBytes();

    /**
     * @return ByteString
     */
    ByteString toByteString();

    /**
     * @return 消息体转字符流
     */
    Reader toCharStream();

    /**
     * @return 消息体转字符串
     */
    String toString();

    /**
     * @return 消息体转Json对象
     */
    Mapper toMapper();

    /**
     * @return 消息体转Json数组
     */
    Array toArray();

    /**
     * @param <T> 目标泛型
     * @param type 目标类型
     * @return 报文体Json文本转JavaBean
     */
    <T> T toBean(Class<T> type);

    /**
     * @param <T> 目标泛型
     * @param type 目标类型
     * @return 报文体Json文本转JavaBean列表
     */
    <T> List<T> toList(Class<T> type);

}
