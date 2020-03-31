package com.ejlchina.http.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import com.ejlchina.http.OnCallback;
import com.ejlchina.http.Process;

public class ProcessInputStream extends InputStream {

	private InputStream input;
	private OnCallback<Process> onProcess;
	private Executor callbackExecutor;
	private long stepBytes;
	private long step = 0;
	private RealProcess process;
	private boolean doneCalled = false;
	
	public ProcessInputStream(InputStream input, OnCallback<Process> onProcess, long totalBytes, long stepBytes,
			long doneBytes, Executor callbackExecutor) {
		this.input = input;
		this.onProcess = onProcess;
		this.stepBytes = stepBytes;
		this.callbackExecutor = callbackExecutor;
		this.process = new RealProcess(totalBytes, doneBytes);
		this.step = doneBytes / stepBytes;
	}


	@Override
	public int read() throws IOException {
		int data = input.read();
		if (data > -1) {
			process.increaseDoneBytes();
		}
		if (process.notDoneOrReached(step * stepBytes)) {
			return data;
		}
		if (process.isDone()) {
			if (doneCalled) {
				return data;
			}
			doneCalled = true;
		}
		step++;
		callbackExecutor.execute(() -> {
			onProcess.on(process);
		});
		return data;
	}

}
