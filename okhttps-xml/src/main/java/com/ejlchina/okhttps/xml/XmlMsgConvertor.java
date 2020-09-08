package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.internal.HttpException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class XmlMsgConvertor implements MsgConvertor, ConvertProvider {

    private String[] nameKeys = {"name", "key"};
    private String[] valueKeys = {"value"};

    private boolean serializeFormatted = false;

    private DocumentBuilderFactory dbFactory;

    public XmlMsgConvertor() {
        this(DocumentBuilderFactory.newInstance());
    }

    public XmlMsgConvertor(DocumentBuilderFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    @Override
    public String mediaType() {
        return "application/xml";
    }

    private Element parseElement(InputStream in, Charset charset) {
        DocumentBuilder builder;
        try {
            // DocumentBuilder 是线程不安全的，所有每次解析都得新起一个 Builder
            builder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new HttpException("DocumentBuilderFactory 配置异常", e);
        }
        try {
            InputSource source = new InputSource(in);
            source.setEncoding(charset.name());
            return builder.parse(source).getDocumentElement();
        } catch (SAXException|IOException e) {
            throw new HttpException("XML 解析异常", e);
        }
    }

    @Override
    public Mapper toMapper(InputStream in, Charset charset) {
        Element root = parseElement(in, charset);
        return new XmlMapper(nameKeys, valueKeys, root);
    }

    @Override
    public Array toArray(InputStream in, Charset charset) {
        Element root = parseElement(in, charset);
        return new XmlArray(nameKeys, valueKeys, root.getChildNodes());
    }

    @Override
    public byte[] serialize(Object object, Charset charset) {
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, serializeFormatted);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, charset.name());
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString().getBytes(charset);
        } catch (JAXBException e) {
            throw new HttpException("XML 序列化异常：", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toBean(Type type, InputStream in, Charset charset) {
        Class<?> clazz = toClass(type);
        if (clazz == null) {
            throw new IllegalStateException("无法获取类型：" + type);
        }
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputSource source = new InputSource(in);
            source.setEncoding(charset.name());
            return (T) unmarshaller.unmarshal(source);
        } catch (Exception e) {
            throw new IllegalStateException("反序列化异常：", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
        Array array = toArray(in, charset);
        List<T> list = new ArrayList<>();
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new IllegalStateException("反序列化异常：", e);
        }
        for (int i = 0; i < array.size(); i++) {
            Mapper mapper = array.getMapper(i);
            try {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                InputSource source = new InputSource(mapper.toString());
                source.setEncoding(charset.name());
                list.add((T) unmarshaller.unmarshal(source));
            } catch (Exception e) {
                throw new IllegalStateException("反序列化异常：" + type);
            }
        }
        return list;
    }

    private Class<?> toClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }
        return null;
    }

    @Override
    public MsgConvertor getConvertor() {
        return new XmlMsgConvertor();
    }

    public String[] getNameKeys() {
        return nameKeys;
    }

    public void setNameKeys(String[] nameKeys) {
        this.nameKeys = nameKeys;
    }

    public String[] getValueKeys() {
        return valueKeys;
    }

    public void setValueKeys(String[] valueKeys) {
        this.valueKeys = valueKeys;
    }

    public DocumentBuilderFactory getDbFactory() {
        return dbFactory;
    }

    public void setDbFactory(DocumentBuilderFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    public boolean isSerializeFormatted() {
        return serializeFormatted;
    }

    public void setSerializeFormatted(boolean serializeFormatted) {
        this.serializeFormatted = serializeFormatted;
    }

}
