package com.ejlchina.http.internal;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.ejlchina.http.OnCallback;
import com.ejlchina.http.Process;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class ProcessRequestBody extends RequestBody {

	private RequestBody requestBody;
	private OnCallback<Process> onProcess;
	private Executor callbackExecutor;
	private long stepBytes;
	private long step = 0;
	private RealProcess process;
	private boolean doneCalled = false;
	
	public ProcessRequestBody(RequestBody requestBody, OnCallback<Process> onProcess, Executor callbackExecutor,
			long contentLength, long stepBytes) {
		this.requestBody = requestBody;
		this.onProcess = onProcess;
		this.callbackExecutor = callbackExecutor;
		this.stepBytes = stepBytes;
		this.process = new RealProcess(contentLength, 0);
	}

	@Override
	public long contentLength() throws IOException {
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

	private BufferedSink bufferedSink;
	
	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		if (bufferedSink == null) {
            bufferedSink = Okio.buffer(new ForwardingSink(sink) {
      
                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                	//这个方法会循环调用，byteCount 是每次调用上传的字节数。
                    super.write(source, byteCount);
                    process.addDoneBytes(byteCount);
            		if (process.notDoneOrReached(step * stepBytes)) {
            			return;
            		}
            		if (process.isDone()) {
            			if (doneCalled) {
            				return;
            			}
            			doneCalled = true;
            		}
            		step++;
            		callbackExecutor.execute(() -> {
            			onProcess.on(process);
            		});
                }
                
            });
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
	}
	

	
}

