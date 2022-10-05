package com.ejlchina.okhttps;

import cn.zhxu.data.Array;
import cn.zhxu.data.DataConvertor;
import cn.zhxu.data.DataSet;
import cn.zhxu.data.Mapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
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
	 * 表单转换器，可用于自动序列化表单参数
	 */
	class FormConvertor implements MsgConvertor {

		private final DataConvertor convertor;
		private final boolean urlEncoded;

		public FormConvertor(DataConvertor convertor) {
			this(convertor, false);
		}

		/**
		 * @param convertor DataConvertor
		 * @param urlEncoded 是否进行 URLEncode 编码
		 */
		public FormConvertor(DataConvertor convertor, boolean urlEncoded) {
			this.convertor = convertor;
			this.urlEncoded = urlEncoded;
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
			byte[] result = convertor.serialize(object, charset);
			Mapper mapper = convertor.toMapper(new ByteArrayInputStream(result), charset);
			StringBuilder sb = new StringBuilder();
			mapper.forEach((key, data) -> {
				String value = encodeValue(data, charset);
				sb.append(key).append('=').append(value).append('&');
			});
			int endIndex = sb.length() > 1 ? sb.length() - 1 : sb.length();
			return sb.substring(0, endIndex).getBytes(charset);
		}

		private String encodeValue(DataSet.Data data, Charset charset) {
			String value = data.toString();
			if (urlEncoded) {
				try {
					return URLEncoder.encode(value, charset.name());
				} catch (UnsupportedEncodingException e) {
					throw new OkHttpsException("UnsupportedEncoding: " + charset.name(), e);
				}
			}
			return value;
		}

		@Override
		public <T> T toBean(Type type, InputStream in, Charset charset) {
			return convertor.toBean(type, in, charset);
		}

		@Override
		public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
			return convertor.toList(type, in, charset);
		}

		public DataConvertor getConvertor() {
			return convertor;
		}

		public boolean isUrlEncoded() {
			return urlEncoded;
		}

	}

}
