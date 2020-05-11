package com.ejlchina.okhttps.internal;

import java.io.InputStream;
import java.util.List;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;

public abstract class AbstractBody {

	
	protected TaskExecutor taskExecutor;
	
	
	public AbstractBody(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public abstract InputStream toByteStream();
	

	public Mapper toMapper() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Mapper 转换！");
		}
		return taskExecutor.convertor().toMapper(toByteStream());
	}


	public Array toArray() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Array 转换！");
		}
		return taskExecutor.convertor().toArray(toByteStream());
	}


	public <T> T toBean(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Bean 转换！");
		}
		return taskExecutor.convertor().toBean(type, toByteStream());
	}
	

	public <T> List<T> toList(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 List 转换！");
		}
		return taskExecutor.convertor().toList(type, toByteStream());
	}
	
}
