package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

public class XmlMsgConvertor implements MsgConvertor {

    @Override
    public String mediaType() {
        return "application/xml";
    }

    @Override
    public Mapper toMapper(InputStream in, Charset charset) {
        return null;
    }

    @Override
    public Array toArray(InputStream in, Charset charset) {
        return null;
    }

    @Override
    public byte[] serialize(Object object, Charset charset) {
        return new byte[0];
    }

    @Override
    public <T> T toBean(Type type, InputStream in, Charset charset) {
        return null;
    }

    @Override
    public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
        return null;
    }

}
