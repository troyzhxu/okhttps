package com.ejlchina.okhttps.internal;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;

public abstract class AbstractBody {

	
	protected TaskExecutor taskExecutor;
	protected Charset charset;
	
	public AbstractBody(TaskExecutor taskExecutor, Charset charset) {
		this.taskExecutor = taskExecutor;
		this.charset = charset;
	}

	public abstract InputStream toByteStream();
	

	public Mapper toMapper() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Mapper 转换！");
		}
		return taskExecutor.convertor().toMapper(toByteStream(), charset);
	}


	public Array toArray() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Array 转换！");
		}
		return taskExecutor.convertor().toArray(toByteStream(), charset);
	}


	public <T> T toBean(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Bean 转换！");
		}
		return taskExecutor.convertor().toBean(type, toByteStream(), charset);
	}
	

	public <T> List<T> toList(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 List 转换！");
		}
		return taskExecutor.convertor().toList(type, toByteStream(), charset);
	}
	
}
