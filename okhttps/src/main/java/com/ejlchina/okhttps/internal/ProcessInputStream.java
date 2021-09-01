package com.ejlchina.okhttps.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.Process;

public class ProcessInputStream extends InputStream {

	private final InputStream input;
	private final OnCallback<Process> onProcess;
	private final Executor callbackExecutor;
	private final long stepBytes;
	private final RealProcess process;
	private boolean doneCalled = false;
	private long step;
	
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
		if (process.isUndoneAndUnreached(step * stepBytes)) {
			return data;
		}
		if (process.isDone()) {
			if (doneCalled) {
				return data;
			}
			doneCalled = true;
		}
		step++;
		callbackExecutor.execute(() -> onProcess.on(process));
		return data;
	}

}
