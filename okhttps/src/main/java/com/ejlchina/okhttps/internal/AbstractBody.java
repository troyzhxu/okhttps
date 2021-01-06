package com.ejlchina.okhttps.internal;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import com.ejlchina.okhttps.*;

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
		return taskExecutor.doMsgConvert(c -> c.toMapper(convertingStream(), charset));
	}

	@Override
	public Array toArray() {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Array 转换！");
		}
		return taskExecutor.doMsgConvert(c -> c.toArray(convertingStream(), charset));
	}

	@Override
	public <T> T toBean(Class<T> type) {
		return doToBean(type);
	}

	@Override
	public <T> T toBean(Type type) {
		return doToBean(type);
	}

	@Override
	public <T> T toBean(TypeRef<T> type) {
		return doToBean(type.getType());
	}

	public <T> T doToBean(Type type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 Bean 转换！");
		}
		return taskExecutor.doMsgConvert(c -> c.toBean(type, convertingStream(), charset));
	}

	@Override
	public <T> List<T> toList(Class<T> type) {
		if (taskExecutor == null) {
			throw new IllegalStateException("没有 taskExecutor，不可做 List 转换！");
		}
		return taskExecutor.doMsgConvert(c -> c.toList(type, convertingStream(), charset));
	}

	/**
	 * @return 待转换的输入流
	 */
	protected InputStream convertingStream() {
		return toByteStream();
	}

}
