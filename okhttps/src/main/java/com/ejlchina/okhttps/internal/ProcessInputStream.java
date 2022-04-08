package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.Process;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ProcessInputStream extends InputStream {

	private final InputStream input;
	private final Consumer<Process> onProcess;
	private final Executor callbackExecutor;
	private final long stepBytes;
	private final RealProcess process;
	private boolean doneCalled = false;
	private long step;
	
	public ProcessInputStream(InputStream input, Consumer<Process> onProcess, long totalBytes, long stepBytes,
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
		byte[] buf = new byte[1];
		int count = read(buf, 0, 1);
		if (count > 0) {
			return buf[0];
		}
		return -1;
	}

	@Override
	public int read(@SuppressWarnings("NullableProblems") byte[] buf, int off, int len) throws IOException {
		int total = 0;
		while (total < len) {
			// 一次读取长度
			int length = Math.min(len - total, (int) stepBytes);
			int read = input.read(buf, off + total, length);
			if (read == -1) {
				// 已经读完
				if (total == 0) {
					return read;
				}
				break;
			}
			updateProcess(read);
			total += read;
		}
		return total;
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
		Process p = process.newProcess();
		callbackExecutor.execute(() -> onProcess.accept(p));
	}

	@Override
	public int available() throws IOException {
		return input.available();
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

}
