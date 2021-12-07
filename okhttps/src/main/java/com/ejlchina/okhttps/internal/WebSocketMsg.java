package com.ejlchina.okhttps.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.ejlchina.okhttps.TaskExecutor;
import com.ejlchina.okhttps.WebSocket.Message;

import okio.ByteString;


public class WebSocketMsg extends AbstractBody implements Message {

	private String text;
	private ByteString bytes;
	
	public WebSocketMsg(String text, TaskExecutor taskExecutor, Charset charset) {
		super(taskExecutor, charset);
		this.text = text;
	}

	public WebSocketMsg(ByteString bytes, TaskExecutor taskExecutor, Charset charset) {
		super(taskExecutor, charset);
		this.bytes = bytes;
	}
	
	@Override
	public boolean isText() {
		return text != null;
	}

	@Override
	public byte[] toBytes() {
		if (text != null) {
			return text.getBytes(StandardCharsets.UTF_8);
		}
		if (bytes != null) {
			return bytes.toByteArray();
		}
		return null;
	}

	@Override
	public String toString() {
		if (text != null) {
			return text;
		}
		if (bytes != null) {
			return bytes.utf8();
		}
		return null;
	}

	@Override
	public ByteString toByteString() {
		if (text != null) {
			return ByteString.encodeUtf8(text);
		}
		return bytes;
	}

	@Override
	public Reader toCharStream() {
		return new InputStreamReader(toByteStream());
	}

	@Override
	public InputStream toByteStream() {
		if (text != null) {
			return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
		}
		if (bytes != null) {
			ByteBuffer buffer = bytes.asByteBuffer();
			return new InputStream() {
				
				@Override
				public int read() {
					if (buffer.hasRemaining()) {
						return buffer.get();
					}
					return -1;
				}
			};
		}
		return null;
	}
	
}
