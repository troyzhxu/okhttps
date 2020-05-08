package com.ejlchina.okhttps;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class FastJsonFactory implements JsonFactory {

	@Override
	public JsonObj newJsonObj(String json) {
		return new FastJsonObj(JSON.parseObject(json));
	}

	@Override
	public JsonArr newJsonArr(String json) {
		return new FastJsonArr(JSON.parseArray(json));
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
		return JSON.parseObject(json, type);
	}

	@Override
	public <T> List<T> jsonToList(Class<T> type, String json) {
		return JSON.parseArray(json, type);
	}

}
