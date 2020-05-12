package com.ejlchina.okhttps.internal;

import java.nio.charset.Charset;
import java.util.List;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.Toable;

public abstract class AbstractBody implements Toable {

	protected TaskExecutor taskExecutor;
	protected Charset charset;

	public AbstractBody(TaskExecutor taskExecutor, Charset charset) {
		this.taskExecutor = taskExecutor;
		this.charset = charset;
	}

	@Override
	public Mapper toMapper() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Mapper 转换！");
		}
		return taskExecutor.doMsgConvert((MsgConvertor c) -> c.toMapper(toByteStream(), charset));
	}

	@Override
	public Array toArray() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Array 转换！");
		}
		return taskExecutor.doMsgConvert((MsgConvertor c) -> c.toArray(toByteStream(), charset));
	}

	@Override
	public <T> T toBean(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Bean 转换！");
		}
		return taskExecutor.doMsgConvert((MsgConvertor c) -> c.toBean(type, toByteStream(), charset));
	}

	@Override
	public <T> List<T> toList(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 List 转换！");
		}
		return taskExecutor.doMsgConvert((MsgConvertor c) -> c.toList(type, toByteStream(), charset));
	}
	
}
