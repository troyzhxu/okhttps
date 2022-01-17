package com.ejlchina.okhttps.internal;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.ejlchina.okhttps.*;
import com.ejlchina.okhttps.HttpResult.Body;

import com.ejlchina.okhttps.Process;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.ByteString;

public class ResultBody extends AbstractBody implements Body {

	private final HttpResult result;
	private final Response response;
	private boolean onIO = false;
	private OnCallback<Process> onProcess;
	private long stepBytes = 0;
	private double stepRate = -1;
	private boolean rangeIgnored = false;
	private boolean cached = false;
	private byte[] data;

	ResultBody(HttpResult result, Response response, TaskExecutor taskExecutor) {
		super(taskExecutor, result.getTask().charset(response));
		this.result = result;
		this.response = response;
	}


	@Override
	public MediaType getType() {
		ResponseBody body = response.body();
		if (body != null) {
			return body.contentType();
		}
		return null;
	}

	@Override
	public long getLength() {
		ResponseBody body = response.body();
		if (body != null) {
			return body.contentLength();
		}
		return 0;
	}

	@Override
    public Body nextOnIO() {
    	onIO = true;
    	return this;
    }

	@Override
	public Body setOnProcess(OnCallback<Process> onProcess) {
		if (taskExecutor == null) {
			response.close();
			throw new IllegalStateException("没有 taskExecutor，不可设置下载进度回调！");
		}
		if (cached) {
			response.close();
			throw new IllegalStateException("开启缓存后，不可设置下载进度回调！");
		}
		this.onProcess = onProcess;
		return this;
	}

	@Override
	public Body stepBytes(long stepBytes) {
		this.stepBytes = stepBytes;
		return this;
	}

	@Override
	public Body stepRate(double stepRate) {
		this.stepRate = stepRate;
		return this;
	}

	@Override
	public Body setRangeIgnored() {
		this.rangeIgnored =true;
		return this;
	}

	@Override
	protected InputStream convertingStream() {
		if (taskExecutor.isMulitMsgConvertor()) {
			// 多个 MsgConvertor 时，自动开启缓存
			cache();
		}
		return toByteStream();
	}

	@Override
	public InputStream toByteStream() {
		InputStream input;
		if (cached) {
			input = new ByteArrayInputStream(cacheBytes());
		} else {
			ResponseBody body = response.body();
			if (body != null) {
				input = body.byteStream();
			} else {
				input = new ByteArrayInputStream(new byte[0]);
			}
		}
		if (onProcess != null) {
			long rangeStart = getRangeStart();
			long totalBytes = getLength();
			if (!rangeIgnored) {
				totalBytes += rangeStart;
			}
			if (stepRate > 0 && stepRate <= 1) {
				stepBytes = (long) (totalBytes * stepRate);
			}
			if (stepBytes <= 0) {
				stepBytes = Process.DEFAULT_STEP_BYTES;
			}
			return new ProcessInputStream(input, onProcess, totalBytes, stepBytes, 
					rangeIgnored ? 0 : rangeStart, taskExecutor.getExecutor(onIO));
		}
		return input;
	}

	@Override
	public byte[] toBytes() {
		if (cached) {
			return cacheBytes();
		}
		return bodyToBytes();
	}

	@Override
	public Reader toCharStream() {
		if (cached || onProcess != null) {
			return new InputStreamReader(toByteStream());
		}
		ResponseBody body = response.body();
		if (body != null) {
			return body.charStream();
		}
		return new CharArrayReader(new char[]{});
	}

	@Override
	public String toString() {
		if (cached || onProcess != null) {
			return new String(toBytes(), charset);
		}
		try {
			ResponseBody body = response.body();
			if (body != null) {
				return new String(body.bytes(), charset);
			}
		} catch (IOException e) {
			response.close();
			throw new OkHttpsException("报文体转化字符串出错", e);
		}
		return null;
	}

	@Override
	public ByteString toByteString() {
		return ByteString.of(toBytes());
	}

	@Override
	public Download toFile(String filePath) {
		return toFile(new File(filePath));
	}

	@Override
	public Download toFile(File file) {
		try {
			if (taskExecutor == null) {
				throw new IllegalStateException("没有 taskExecutor， 不可进行下载操作！");
			}
			if (!file.exists()) {
				File parent = file.getParentFile();
				if (parent == null) {
					throw new IllegalStateException("不正确的下载路径：" + file.getPath());
				}
				if (!parent.exists() && !parent.mkdirs()) {
					throw new IllegalStateException("不能创建父目录：" + parent.getPath());
				}
				if (!file.createNewFile()) {
					throw new IllegalStateException("文件刚被其它线程占用：" + parent.getPath());
				}
			}
		} catch (Exception e) {
			response.close();
			throw new OkHttpsException("文件下载失败", e);
		}
		return taskExecutor.download(result.getTask(), file, toByteStream(), getRangeStart());
	}
	
	@Override
	public Download toFolder(String dirPath) {
		DownloadHelper helper = result.getTask().httpClient().downloadHelper;
		String fileName = helper.resolveFileName(result);
		String filePath = helper.resolveFilePath(dirPath, fileName);
		int index = 0;
		File file = new File(filePath);
		while (file.exists()) {
			String indexFileName = helper.indexFileName(fileName, index++);
			filePath = helper.resolveFilePath(dirPath, indexFileName);
			file = new File(filePath);
		}
		return toFile(file);
	}

	@Override
	public Download toFolder(File dir) {
		try {
			if (dir.exists() && !dir.isDirectory()) {
				throw new IllegalStateException("文件[" + dir.getPath() + "]已存在，并且不是一个目录！");
			}
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IllegalStateException("不能创建目录：" + dir.getPath());
			}
		} catch (Exception e) {
			response.close();
			throw new OkHttpsException("目录创建失败", e);
		}
		return toFolder(dir.getAbsolutePath());
	}
	
	@Override
	public Body cache() {
		if (onProcess != null) {
			response.close();
			throw new IllegalStateException("设置了下载进度回调，不可再开启缓存！");
		}
		cached = true;
		return this;
	}
	
	@Override
	public Body close() {
		response.close();
		data = null;
		return this;
	}
	
	private byte[] cacheBytes() {
		synchronized (response) {
			if (data == null) {
				data = bodyToBytes();
			}
		}
		return data;
	}
	
	private byte[] bodyToBytes() {
		if (onProcess != null) {
			try (Buffer buffer = new Buffer()) {
				return buffer.readFrom(toByteStream()).readByteArray();
			} catch (IOException e) {
				throw new OkHttpsException("报文体转化字节数组出错", e);
			} finally {
				response.close();
			}
		}
		ResponseBody body = response.body();
		if (body != null) {
			try {
				return body.bytes();
			} catch (IOException e) {
				body.close();
				throw new OkHttpsException("报文体转化字节数组出错", e);
			}
		}
		return new byte[0];
	}
	
	private long getRangeStart() {
		long rangeStart = 0;
		if (response.code() != HttpURLConnection.HTTP_PARTIAL) {
			return rangeStart;
		}
		String range = response.header("Content-Range");
		if (range != null && range.startsWith("bytes")) {
			int index = range.indexOf('-');
			if (index > 5) {
				String start = range.substring(5, index).trim();
				try {
					rangeStart = Long.parseLong(start);
				} catch (Exception ignore) {}
			}
		}
		return rangeStart;
	}

}
