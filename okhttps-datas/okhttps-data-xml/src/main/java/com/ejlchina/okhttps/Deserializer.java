package com.ejlchina.okhttps;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deserializer {

    public Object deserialize(Mapper mapper, Type type) {
        Type[] typeArgs = null;
        if (type instanceof ParameterizedType) {
            typeArgs = ((ParameterizedType) type).getActualTypeArguments();
        }
        Class<?> clazz = toClass(type);
        return toBean(clazz, typeArgs, mapper);
    }

    protected Object toBean(Class<?> clazz, Type[] typeArgs, Mapper mapper) {
        if (clazz == Map.class || clazz == HashMap.class) {
            Map<String, Object> map = new HashMap<>();
            for (String key : mapper.keySet()) {
                if (typeArgs.length > 1) {
                    map.put(key, fieldValue(mapper, key, typeArgs[1]));
                } else {
                    map.put(key, fieldValue(mapper, key, String.class));
                }
            }
            return map;
        }
        Object bean;
        try {
            bean = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("无法构造 " + clazz + " 对象", e);
        }
        TypeVariable<?>[] typeParas = clazz.getTypeParameters();
        Map<String, Method> methods = getSetMehthods(clazz);
        for (String field : methods.keySet()) {
            Method method = methods.get(field);
            Type fieldType = method.getParameterTypes()[0];
            if (fieldType == Object.class && typeParas.length > 0) {
                Type[] gTypes = method.getGenericParameterTypes();
                if (gTypes.length > 0) {
                    Type gType = gTypes[0];
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

    protected Object fieldValue(Mapper mapper, String field, Type type) {
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
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    list.add(deserialize(array.getMapper(i), ((ParameterizedType) type).getActualTypeArguments()[0]));
                }
                return list;
            }
        } else {
            Mapper value = mapper.getMapper(field);
            if (value != null) {
                return deserialize(value, type);
            }
        }
        return null;
    }

    protected Map<String, Method> getSetMehthods(Class<?> clazz) {
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

    protected Class<?> toClass(Type type) {
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

}
