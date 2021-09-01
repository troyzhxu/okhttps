package com.ejlchina.okhttps.internal;

import com.ejlchina.okhttps.Process;

public class RealProcess implements Process {

	// 总字节数
	private final long totalBytes;
	// 已经完成字节数
	private long doneBytes;
	
	
	public RealProcess(long totalBytes, long doneBytes) {
		this.totalBytes = totalBytes;
		this.doneBytes = doneBytes;
	}

	public Process clone() {
		return new RealProcess(totalBytes, doneBytes);
	}

	@Override
	public double getRate() {
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
		return doneBytes >= totalBytes;
	}
	
	public void addDoneBytes(long delt) {
		doneBytes += delt;
	}
	
	public void increaseDoneBytes() {
		doneBytes++;
	}
	
	public boolean isUndoneAndUnreached(long bytes) {
		return doneBytes < bytes && doneBytes < totalBytes;
	}

	@Override
	public String toString() {
		return "Process[" + doneBytes + " / " + totalBytes + " | " + getRate() + ']';
	}
}
