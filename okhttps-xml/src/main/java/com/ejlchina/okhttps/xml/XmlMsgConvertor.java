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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

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
    public <T> T toBean(Type type, InputStream in, Charset charset) {
        return toBean(type, toMapper(in, charset));
    }

    @Override
    public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
        Array array = toArray(in, charset);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(toBean(type, array.getMapper(i)));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private <T> T toBean(Type type, Mapper mapper) {
        Type[] typeArgs = null;
        if (type instanceof ParameterizedType) {
            typeArgs = ((ParameterizedType) type).getActualTypeArguments();
        }
        Class<T> clazz = (Class<T>) toClass(type);
        return toBean(clazz, typeArgs, mapper);
    }

    @SuppressWarnings("unchecked")
    private <T> T toBean(Class<T> clazz, Type[] typeArgs, Mapper mapper) {
        if (clazz == Map.class || clazz == HashMap.class) {
            Map<String, Object> map = new HashMap<>();
            for (String key : mapper.keySet()) {
                if (typeArgs.length > 1) {
                    map.put(key, fieldValue(mapper, key, typeArgs[1]));
                } else {
                    map.put(key, fieldValue(mapper, key, String.class));
                }
            }
            return (T) map;
        }
        T bean;
        try {
            bean = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("无法构造 " + clazz + " 对象", e);
        }
        TypeVariable<Class<T>>[] typeParas = clazz.getTypeParameters();
        Map<String, Method> methods = getSetMehthods(clazz);
        for (String field : methods.keySet()) {
            Method method = methods.get(field);
            Type fieldType = method.getParameterTypes()[0];
            if (fieldType == Object.class && typeParas != null && typeParas.length > 0) {
                Type[] gpts = method.getGenericParameterTypes();
                if (gpts != null && gpts.length > 0) {
                    Type gType = gpts[0];
                    for (int i = 0; i < typeParas.length; i++) {
                        if (typeParas[i] == gType) {
                            fieldType = typeArgs[i];
                        }
                    }
                }
            }
            Object fieldValue = fieldValue(mapper, field, fieldType);
            try {
                method.invoke(bean, fieldValue);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("无法为 " + clazz + " 对象的 " + field + "属性赋值", e);
            }
        }
        return bean;
    }

    private Object fieldValue(Mapper mapper, String field, Type type) {
        if (type == int.class || type == Integer.class) {
            return mapper.getInt(field);
        }
        if (type == long.class || type == Long.class) {
            return mapper.getLong(field);
        }
        if (type == float.class || type == Float.class) {
            return mapper.getFloat(field);
        }
        if (type == double.class || type == Double.class) {
            return mapper.getDouble(field);
        }
        if (type == boolean.class || type == Boolean.class) {
            return mapper.getBool(field);
        }
        if (type == String.class) {
            return mapper.getString(field);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(mapper.getString(field));
        }
        if (type == BigInteger.class) {
            return new BigInteger(mapper.getString(field));
        }
        Class<?> clazz = toClass(type);
        if (clazz == List.class || clazz == ArrayList.class) {
            Array array = mapper.getArray(field);
            if (array != null) {
                List<?> list = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    list.add(toBean(((ParameterizedType) type).getActualTypeArguments()[0], array.getMapper(i)));
                }
                return list;
            }
        } else {
            Mapper value = mapper.getMapper(field);
            if (value != null) {
                return toBean(type, value);
            }
        }
        return null;
    }

    private Map<String, Method> getSetMehthods(Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        for (Method method: clazz.getMethods()) {
            String methodName = method.getName();
            Class<?>[] paraTypes = method.getParameterTypes();
            if (paraTypes.length != 1 || methodName.length() <= 3
                    || !methodName.startsWith("set")) {
                continue;
            }
            String field = XmlUtils.firstCharToLowerCase(methodName.substring(3));
            methods.put(field, method);
        }
        return methods;
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
