package com.ejlchina.okhttps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ejlchina.okhttps.internal.RealHttpResult;
import com.ejlchina.okhttps.internal.TaskExecutor;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;


/**
 * Http 执行结果
 */
public interface HttpResult {

	/**
	 * 构造一个 HttpResult
	 * 此方法构造的 HttpResult 不可设置进度回调，不可进行下载操作！
	 * 若需要，请使用方法： {@link #of(Response, TaskExecutor)}
	 * @param response Response
	 * @return HttpResult
	 */
	static HttpResult of(Response response) {
		return of(response, null);
	}
	
	/**
	 * 构造一个 HttpResult
	 * @param response Response
	 * @param taskExecutor 任务执行器, 可通过方法 {@link HTTP#getExecutor()} 获得
	 * @return HttpResult
	 */
	static HttpResult of(Response response, TaskExecutor taskExecutor) {
		if (response != null) {
			return new RealHttpResult(null, response, taskExecutor);
		}
		throw new IllegalArgumentException("response 不能为空");
	}

	
	public enum State {
		
		/**
		 * 执行异常
		 */
	    EXCEPTION,
	    
	    /**
	     * 请求被取消
	     */
	    CANCELED,
	    
	    /**
	     * 请求已响应
	     */
	    RESPONSED,
	    
	    /**
	     * 网络超时
	     */
	    TIMEOUT,
	    
	    /**
	     * 网络出错
	     */
	    NETWORK_ERROR
		
	}
	
	/**
	 * HTTP响应报文体
	 */
	public interface Body {
		
		/**
		 * @return 媒体类型
		 */
		MediaType getContentType();
		
		/**
		 * @return 报文体字节长度
		 */
		long getContentLength();

	    /**
	     * 在IO线程执行
	     * @return Body
	     */
		Body nextOnIO();
		
		/**
		 * 设置报文体接收进度回调
		 * @param onProcess 进度回调函数
		 * @return Body
		 */
		Body setOnProcess(OnCallback<Process> onProcess);
		
		/**
		 * 设置进度回调的步进字节，默认 8K（8192）
		 * 表示每接收 stepBytes 个字节，执行一次进度回调
		 * @param stepBytes 步进字节
		 * @return Body 
		 */
		Body setStepBytes(long stepBytes);
		
		/**
		 * 设置进度回调的步进比例
		 * 表示每接收 stepRate 比例，执行一次进度回调
		 * @param stepRate 步进比例
		 * @return Body
		 */
		Body setStepRate(double stepRate);
		
		/**
		 * 设置进度回调忽略响应的Range头信息，即进度回调会从0开始
		 * @return Body
		 */
		Body setRangeIgnored();
		
		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字节流
		 */
		InputStream toByteStream();
		
		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字节数组
		 */
		byte[] toBytes();
		
		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字符流
		 */
		Reader toCharStream();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转字符串
		 */
		String toString();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转Json对象
		 */
		JSONObject toJsonObject();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @return 报文体转Json数组
		 */
		JSONArray toJsonArray();

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param <T> 目标泛型
		 * @param type 目标类型
		 * @return 报文体Json文本转JavaBean
		 */
		<T> T toBean(Class<T> type);

		/**
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param <T> 目标泛型
		 * @param type 目标类型
		 * @return 报文体Json文本转JavaBean列表
		 */
		<T> List<T> toList(Class<T> type);
		
		/**
		 * 下载到指定路径
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param filePath 目标路径
		 * @return 下载过程 #Download
		 */
		Download toFile(String filePath);

		/**
		 * 下载到指定文件
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param file 目标文件
		 * @return 下载过程 #Download
		 */
		Download toFile(File file);
	
		/**
		 * 下载到指定文件夹
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param dirPath 目标目录
		 * @return 下载过程 #Download
		 */
		Download toFolder(String dirPath);

		/**
		 * 下载到指定文件夹
		 * 同一个 Body 对象的 toXXX 类方法只可使用一个并且只能调用一次
		 * @param dir 目标目录
		 * @return 下载过程 #Download
		 */
		Download toFolder(File dir);

		/**
		 * 缓存自己，缓存后可 重复使用 toXXX 类方法
		 * @return Body
		 */
		Body cache();
		
		/**
		 * 关闭报文体
		 * 未对报文体做任何消费时使用，比如只读取长度
		 * @return Body
		 */
		Body close();
		
	}
	

	/**
	 * @return 执行状态
	 */
	State getState();

	/**
	 * @return HTTP状态码
	 */
	int getStatus();

	/**
	 * @return 是否响应成功，状态码在 [200..300) 之间
	 */
	boolean isSuccessful();
	
	/**
	 * @return 响应头
	 */
	Headers getHeaders();

	/**
	 * @param name 头名称
	 * @return 响应头
	 */
	List<String> getHeaders(String name);

	/**
	 * @param name 头名称
	 * @return 响应头
	 */
	String getHeader(String name);
	
	/**
	 * @return 响应报文体
	 */
	Body getBody();
	
	/**
	 * @return 执行中发生的异常
	 */
	IOException getError();

	/**
	 * 关闭报文
	 * 未对报文体做任何消费时使用，比如只读取报文头
	 * @return HttpResult
	 */
	HttpResult close();

}
