package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.Process;

public class RealProcess implements Process {

	// 总字节数（流式上传时该字段为 -1）
	private final long totalBytes;
	// 已经完成字节数
	private long doneBytes;

	public RealProcess(long totalBytes, long doneBytes) {
		this.totalBytes = totalBytes;
		this.doneBytes = doneBytes;
	}

	public Process newProcess() {
		return new RealProcess(totalBytes, doneBytes);
	}

	@Override
	public double getRate() {
		if (totalBytes == 0) {
			return 1;
		}
		if (totalBytes < 0) {
			return -1;
		}
		return (double) doneBytes / totalBytes;
	}

	@Override
	public long getTotalBytes() {
		return totalBytes;
	}

	@Override
	public long getDoneBytes() {
		return doneBytes;
	}
	
	@Override
	public boolean isDone() {
		return doneBytes >= totalBytes && totalBytes >= 0;
	}
	
	public void addDoneBytes(long delt) {
		doneBytes += delt;
	}

	public boolean isUndoneAndUnreached(long bytes) {
		return doneBytes < bytes && (totalBytes < 0 || doneBytes < totalBytes);
	}

	@Override
	public String toString() {
		return "Process[" + doneBytes + " / " + totalBytes + " | " + getRate() + ']';
	}

}
