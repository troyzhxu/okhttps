package com.ejlchina.okhttps.internal;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.Process;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.*;

public class ProcessRequestBody extends RequestBody {

	private final RequestBody requestBody;
	private final OnCallback<Process> onProcess;
	private final Executor callbackExecutor;
	private final RealProcess process;
	private final long stepBytes;
	
	public ProcessRequestBody(RequestBody requestBody, OnCallback<Process> onProcess, Executor callbackExecutor,
			long contentLength, long stepBytes) {
		this.requestBody = requestBody;
		this.onProcess = onProcess;
		this.callbackExecutor = callbackExecutor;
		this.stepBytes = stepBytes;
		this.process = new RealProcess(contentLength, 0);
	}

	class ProcessableSink extends ForwardingSink {

		private long step = 0;
		private boolean doneCalled = false;

		public ProcessableSink(Sink delegate) {
			super(delegate);
		}

		@Override
		public void write(Buffer source, long byteCount) throws IOException {
			// byteCount 可能很大，也可能很小
			long written = 0;
			while (written < byteCount) {
				long count = Math.min(stepBytes, byteCount - written);
				try {
					super.write(source, count);
				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
					throw new IOException("请求体写出异常", e);
				}
				updateProcess(count);
				written += count;
			}
		}

		private void updateProcess(long count) {
			process.addDoneBytes(count);
			if (process.isUndoneAndUnreached(step * stepBytes)) {
				return;
			}
			if (process.isDone()) {
				if (doneCalled) {
					return;
				}
				doneCalled = true;
			}
			step = (process.getDoneBytes() - 1) / stepBytes + 1;
			// 因为 process 一直被更新，所有此处应克隆一个新的对象用于回调
			Process p = process.clone();
			callbackExecutor.execute(() -> onProcess.on(p));
		}

	}

	@Override
	public long contentLength() {
		return process.getTotalBytes();
	}

	@Override
	public boolean isDuplex() {
		return requestBody.isDuplex();
	}

	@Override
	public boolean isOneShot() {
		return requestBody.isOneShot();
	}

	@Override
	public MediaType contentType() {
		return requestBody.contentType();
	}

	@Override
	public void writeTo(@SuppressWarnings("NullableProblems") BufferedSink sink) throws IOException {
		BufferedSink buffer = Okio.buffer(new ProcessableSink(sink));
		requestBody.writeTo(buffer);
		buffer.flush();
	}

}
