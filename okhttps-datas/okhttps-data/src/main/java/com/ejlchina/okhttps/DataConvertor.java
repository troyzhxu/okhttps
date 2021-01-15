package com.ejlchina.okhttps;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 数据转换器
 * @since 2.5.2
 */
public interface DataConvertor {

    /**
     * 解析 Mapper
     * @param in JSON 输入流
     * @param charset 编码格式
     * @return Mapper
     */
    Mapper toMapper(InputStream in, Charset charset);

    /**
     * 解析 Array
     * @param in JSON 输入流
     * @param charset 编码格式
     * @return Array
     */
    Array toArray(InputStream in, Charset charset);

    /**
     * 将 Java 对象序列化为字节数组
     * @param object Java 对象
     * @param charset 编码格式
     * @return 字节数组
     */
    byte[] serialize(Object object, Charset charset);

    /**
     * 解析 Java Bean
     * @param <T> 目标泛型
     * @param type 目标类型
     * @param in JSON 输入流
     * @param charset 编码格式
     * @return Java Bean
     */
    <T> T toBean(Type type, InputStream in, Charset charset);

    /**
     * 解析为 Java List
     * @param <T> 目标泛型
     * @param type 目标类型
     * @param in JSON 输入流
     * @param charset 编码格式
     * @return Java List
     */
    <T> List<T> toList(Class<T> type, InputStream in, Charset charset);

}
