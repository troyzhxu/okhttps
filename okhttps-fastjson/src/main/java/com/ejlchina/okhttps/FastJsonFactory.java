package com.ejlchina.okhttps;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class FastJsonFactory implements JsonFactory {

	@Override
	public JsonObj newJsonObj(String json) {
		if (json != null) {
			return new FastJsonObj(JSON.parseObject(json));
		}
		return null;
	}

	@Override
	public JsonArr newJsonArr(String json) {
		if (json != null) {
			return new FastJsonArr(JSON.parseArray(json));
		}
		return null;
	}

	@Override
	public String toJsonStr(Object bean) {
		return JSON.toJSONString(bean);
	}

	@Override
	public String toJsonStr(Object bean, String dateFormat) {
		return JSON.toJSONStringWithDateFormat(bean, dateFormat);
	}

	@Override
	public <T> T jsonToBean(Class<T> type, String json) {
		if (json != null) {
			return JSON.parseObject(json, type);
		}
		return null;
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, String json) {
		if (json != null) {
			return JSON.parseArray(json, type);
		}
		return null;
	}

}
