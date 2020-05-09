package com.ejlchina.okhttps.internal;

import java.io.InputStream;
import java.util.List;

import com.ejlchina.okhttps.JsonArr;
import com.ejlchina.okhttps.JsonObj;

public abstract class AbstractBody {

	
	protected TaskExecutor taskExecutor;
	
	
	public AbstractBody(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public abstract InputStream toByteStream();
	

	public JsonObj toJsonObj() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Json 转换！");
		}
		return taskExecutor.jsonServiceNotNull().toJsonObj(toByteStream());
	}


	public JsonArr toJsonArr() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Json 转换！");
		}
		return taskExecutor.jsonServiceNotNull().toJsonArr(toByteStream());
	}


	public <T> T toBean(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Json 转换！");
		}
		return taskExecutor.jsonServiceNotNull().jsonToBean(type, toByteStream());
	}
	

	public <T> List<T> toList(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Json 转换！");
		}
		return taskExecutor.jsonServiceNotNull().jsonToList(type, toByteStream());
	}
	
}
