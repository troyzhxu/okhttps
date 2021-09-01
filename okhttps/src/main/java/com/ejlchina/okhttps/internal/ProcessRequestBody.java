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
			//这个方法会循环调用，byteCount 是每次调用上传的字节数。
			super.write(source, byteCount);
			process.addDoneBytes(byteCount);
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
			callbackExecutor.execute(() -> onProcess.on(process));
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
		try {
			requestBody.writeTo(Okio.buffer(new ProcessableSink(sink)));
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
