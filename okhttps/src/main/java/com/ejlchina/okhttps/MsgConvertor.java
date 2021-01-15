package com.ejlchina.okhttps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 消息转换器接口
 */
public interface MsgConvertor extends DataConvertor {

	/**
	 * 消息的媒体类型
	 * @return 媒体类型
	 */
	String mediaType();

	/**
	 * 表单转换器，可用于自动系列化表单参数
	 */
	class FormConvertor implements MsgConvertor {

		private final DataConvertor convertor;

		public FormConvertor(DataConvertor convertor) {
			this.convertor = convertor;
		}

		@Override
		public String mediaType() {
			return "application/x-www-form-urlencoded";
		}

		@Override
		public Mapper toMapper(InputStream in, Charset charset) {
			return convertor.toMapper(in, charset);
		}

		@Override
		public Array toArray(InputStream in, Charset charset) {
			return convertor.toArray(in, charset);
		}

		@Override
		public byte[] serialize(Object object, Charset charset) {
			byte[] data = convertor.serialize(object, charset);
			Mapper mapper = convertor.toMapper(new ByteArrayInputStream(data), charset);
			StringBuilder sb = new StringBuilder();
			for (String key: mapper.keySet()) {
				sb.append(key).append('=').append(mapper.getString(key)).append('&');
			}
			if (sb.length() > 1) {
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString().getBytes(charset);
		}

		@Override
		public <T> T toBean(Type type, InputStream in, Charset charset) {
			return convertor.toBean(type, in, charset);
		}

		@Override
		public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
			return convertor.toList(type, in, charset);
		}

	}

}
