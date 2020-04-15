package com.ejlchina.okhttps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.HttpClient;
import com.ejlchina.okhttps.internal.HttpClient.TagTask;
import com.ejlchina.okhttps.internal.HttpException;
import com.ejlchina.okhttps.internal.ProcessRequestBody;
import com.ejlchina.okhttps.internal.RealHttpResult;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.Buffer;


/**
 * Created by 周旭（Troy.Zhou） on 2020/3/11.
 */
@SuppressWarnings("unchecked")
public abstract class HttpTask<C extends HttpTask<?>> implements Cancelable {

    private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";

    protected HttpClient httpClient;
    protected boolean nothrow;
    protected boolean nextOnIO = false;
    
    private String urlPath;
    private String tag;
    private Map<String, String> headers;
    private Map<String, String> pathParams;
    private Map<String, String> urlParams;
    private Map<String, String> bodyParams;
    private Map<String, Object> jsonParams;
    private Map<String, FilePara> files;
    private String requestJson;
    private OnCallback<Process> onProcess;
    private boolean pOnIO;
    private long stepBytes = 0;
    private double stepRate = -1;

    private Object object;
    
    private TagTask tagTask;
    private Cancelable canceler;


    public HttpTask(HttpClient httpClient, String url) {
        this.httpClient = httpClient;
        this.urlPath = url;
    }

    /**
     * 获取请求任务的URL地址
     * @return URL地址
     */
    public String getUrl() {
        return urlPath;
    }

    /**
     * 获取请求任务的标签
     * @return 标签
     */
    public String getTag() {
        return tag;
    }

    /**
     * 标签匹配
     * 判断任务标签与指定的标签是否匹配（包含指定的标签）
     * @param tag 标签
     * @return 是否匹配
     */
    public boolean isTagged(String tag) {
        if (this.tag != null && tag != null) {
            return this.tag.contains(tag);
        }
        return false;
    }

    /**
     * 获取请求任务的头信息
     * @return 头信息
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 获得被绑定的对象
     * @return Object
     */
    public Object getBound() {
        return object;
    }

    /**
     * 设置在发生异常时不向上抛出，设置后：
     * 异步请求可以在异常回调内捕获异常，同步请求在返回结果中找到该异常
     * @return HttpTask 实例
     */
    public C nothrow() {
        this.nothrow = true;
        return (C) this;
    }

    /**
     * 为请求任务添加标签
     * v1.0.4 之后，若 set 多次，标签将连接在一起
     * @param tag 标签
     * @return HttpTask 实例
     */
    public C setTag(String tag) {
        if (tag != null) {
        	if (this.tag != null) {
        		this.tag = this.tag + "." + tag;
        	} else {
        		this.tag = tag;
        	}
        	updateTagTask();
        }
        return (C) this;
    }
    
    /**
     * 下一个回调在IO线程执行
     * @return HttpTask 实例
     */
    public C nextOnIO() {
        nextOnIO = true;
        return (C) this;
    }

    /**
     * 绑定一个对象
     * @param object 对象
     * @return HttpTask 实例
     */
    public C bind(Object object) {
        this.object = object;
        return (C) this;
    }

    /**
     * 添加请求头
     * @param name 请求头名
     * @param value 请求头值
     * @return HttpTask 实例
     */
    public C addHeader(String name, String value) {
        if (name != null && value != null) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
        }
        return (C) this;
    }

    /**
     * 添加请求头
     * @param headers 请求头集合
     * @return HttpTask 实例
     */
    public C addHeader(Map<String, String> headers) {
        if (headers != null) {
            if (this.headers == null) {
                this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
        }
        return (C) this;
    }

    /**
     * 设置Range头信息
     * 表示接收报文体时跳过的字节数，用于断点续传
     * @param rangeStart 表示从 rangeStart 个字节处开始接收，通常是已经下载的字节数，即上次的断点）
     * @return HttpTask 实例
     */
    public C setRange(long rangeStart) {
        return addHeader("Range", "bytes=" + rangeStart + "-");
    }

    /**
     * 设置Range头信息
     * 设置接收报文体时接收的范围，用于分块下载
     * @param rangeStart 表示从 rangeStart 个字节处开始接收
     * @param rangeEnd 表示接收到 rangeEnd 个字节处
     * @return HttpTask 实例
     */
    public C setRange(long rangeStart, long rangeEnd) {
        return addHeader("Range", "bytes=" + rangeStart + "-" + rangeEnd);
    }

    /**
     * 设置报文体发送进度回调
     * @param onProcess 进度回调函数
     * @return HttpTask 实例
     */
    public C setOnProcess(OnCallback<Process> onProcess) {
        this.onProcess = onProcess;
        pOnIO = nextOnIO;
        nextOnIO = false;
        return (C) this;
    }

    /**
     * 设置进度回调的步进字节，默认 8K（8192）
     * 表示每接收 stepBytes 个字节，执行一次进度回调
     * @param stepBytes 步进字节
     * @return HttpTask 实例
     */
    public C setStepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return (C) this;
    }

    /**
     * 设置进度回调的步进比例
     * 表示每接收 stepRate 比例，执行一次进度回调
     * @param stepRate 步进比例
     * @return HttpTask 实例
     */
    public C setStepRate(double stepRate) {
        this.stepRate = stepRate;
        return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addPathParam(String name, Object value) {
        if (name != null && value != null) {
            if (pathParams == null) {
                pathParams = new HashMap<>();
            }
            pathParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addPathParam(Map<String, ?> params) {
        if (pathParams == null) {
            pathParams = new HashMap<>();
        }
        doAddParams(pathParams, params);
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addUrlParam(String name, Object value) {
        if (name != null && value != null) {
            if (urlParams == null) {
                urlParams = new HashMap<>();
            }
            urlParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addUrlParam(Map<String, ?> params) {
        if (urlParams == null) {
            urlParams = new HashMap<>();
        }
        doAddParams(urlParams, params);
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addBodyParam(String name, Object value) {
        if (name != null && value != null) {
            if (bodyParams == null) {
                bodyParams = new HashMap<>();
            }
            bodyParams.put(name, value.toString());
        }
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addBodyParam(Map<String, ?> params) {
        if (bodyParams == null) {
            bodyParams = new HashMap<>();
        }
        doAddParams(bodyParams, params);
        return (C) this;
    }

    private void doAddParams(Map<String, String> taskParams, Map<String, ?> params) {
        if (params != null) {
            params.forEach((String name, Object value) -> {
                if (name != null && value != null) {
                    taskParams.put(name, value.toString());
                }
            });
        }
    }

    /**
     * Json参数：请求体为Json，支持多层结构
     * @param name JSON键名
     * @param value JSON键值
     * @return HttpTask 实例
     */
    public C addJsonParam(String name, Object value) {
        if (name != null && value != null) {
            if (jsonParams == null) {
                jsonParams = new HashMap<>();
            }
            jsonParams.put(name, value);
        }
        return (C) this;
    }

    /**
     * Json参数：请求体为Json，只支持单层Json
     * 若请求json为多层结构，请使用setRequestJson方法
     * @param params JSON键值集合
     * @return HttpTask 实例
     */
    public C addJsonParam(Map<String, ?> params) {
        if (params != null) {
            if (jsonParams == null) {
                jsonParams = new HashMap<>();
            }
            jsonParams.putAll(params);
        }
        return (C) this;
    }

    /**
     * 设置 json 请求体
     * @param json JSON字符串 或 Java对象（将依据 对象的get方法序列化为 json 字符串）
     * @return HttpTask 实例
     **/
    public C setRequestJson(Object json) {
        return setRequestJson(json, null);
    }

    /**
     * 请求体为json
     * @param json JSON字符串 或 Java对象，将跟换 bean的get方法序列化程 json 字符串
     * @param dateFormat 序列化json时对日期类型字段的处理格式
     * @return HttpTask 实例
     **/
    public C setRequestJson(Object json, String dateFormat) {
        if (json != null) {
            if (json instanceof String) {
                requestJson = json.toString();
            } else if (dateFormat != null) {
                requestJson = JSON.toJSONStringWithDateFormat(json, dateFormat);
            } else {
                requestJson = JSON.toJSONString(json);
            }
        }
        return (C) this;
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param filePath 文件路径
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String filePath) {
        return addFileParam(name, new File(filePath));
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param file 文件
     * @return HttpTask 实例
     */
    public C addFileParam(String name, File file) {
        if (name != null && file != null && file.exists()) {
            String fileName = file.getName();
            String type = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (files == null) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, fileName, file));
        }
        return (C) this;
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param inputStream 文件输入流
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, InputStream inputStream) {
        return addFileParam(name, type, null, inputStream);
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param input 文件输入流
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, String fileName, InputStream input) {
        if (name != null && input != null) {
            byte[] content = null;
            try {
                Buffer buffer = new Buffer();
                content = buffer.readFrom(input).readByteArray();
                buffer.close();
            } catch (IOException e) {
                throw new HttpException("读取文件输入流出错：", e);
            } finally {
                Util.closeQuietly(input);
            }
            addFileParam(name, type, fileName, content);
        }
        return (C) this;
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, byte[] content) {
        return addFileParam(name, type, null, content);
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, String fileName, byte[] content) {
        if (name != null && content != null) {
            if (files == null) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, fileName, content));
        }
        return (C) this;
    }

    @Override
    public boolean cancel() {
        if (canceler != null) {
            return canceler.cancel();
        }
        return false;
    }

    static class FilePara {

        String type;
        String fileName;
        byte[] content;
        File file;

        FilePara(String type, String fileName, byte[] content) {
            this.type = type;
            this.fileName = fileName;
            this.content = content;
        }

        FilePara(String type, String fileName, File file) {
            this.type = type;
            this.fileName = fileName;
            this.file = file;
        }

    }
    
    protected void registeTagTask(Cancelable canceler) {
        if (tag != null) {
        	tagTask = httpClient.addTagTask(tag, canceler, this);
        }
        this.canceler = canceler;
    }

    private void updateTagTask() {
        if (tagTask != null) {
        	tagTask.setTag(tag);
        } else 
        if (canceler != null) {
        	registeTagTask(canceler);
        }
    }
    
    protected void removeTagTask() {
        if (tag != null) {
            httpClient.removeTagTask(this);
        }
    }

    protected Call prepareCall(String method) {
        assertNotConflict("GET".equals(method));
        Request.Builder builder = new Request.Builder()
                .url(buildUrlPath());
        buildHeaders(builder);
        RequestBody reqBody = null;
        if (!"GET".equals(method)) {
            reqBody = buildRequestBody();
            if (onProcess != null) {
                long contentLength = contentLength(reqBody);
                if (stepRate > 0 && stepRate <= 1) {
                    stepBytes = (long) (contentLength * stepRate);
                }
                if (stepBytes <= 0) {
                    stepBytes = Process.DEFAULT_STEP_BYTES;
                }
                reqBody = new ProcessRequestBody(reqBody, onProcess,
                        httpClient.getExecutor().getExecutor(pOnIO),
                        contentLength, stepBytes);
            }
        }
        switch (method) {
            case "GET":
                builder.get();
                break;
            case "POST":
                builder.post(reqBody);
                break;
            case "PUT":
                builder.put(reqBody);
                break;
            case "DELETE":
                builder.delete(reqBody);
                break;
        }
        return httpClient.request(builder.build());
    }

    private long contentLength(RequestBody reqBody) {
        try {
            return reqBody.contentLength();
        } catch (IOException e) {
            throw new HttpException("无法获取请求体长度", e);
        }
    }

    private void buildHeaders(Request.Builder builder) {
        if (headers != null) {
            for (String name : headers.keySet()) {
                String value = headers.get(name);
                if (value != null) {
                    builder.addHeader(name, value);
                }
            }
        }
    }

    protected State toState(IOException e, boolean sync) {
        if (e instanceof SocketTimeoutException) {
            return State.TIMEOUT;
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return State.NETWORK_ERROR;
        }
        String msg = e.getMessage();
        if (msg != null && ("Canceled".equals(msg)
                || sync && e instanceof SocketException
                && msg.startsWith("Socket operation on nonsocket"))) {
            return State.CANCELED;
        }
        return State.EXCEPTION;
    }

    private RequestBody buildRequestBody() {
        if (jsonParams != null) {
            requestJson = JSON.toJSONString(jsonParams);
        }
        if (files != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (bodyParams != null) {
                for (String name : bodyParams.keySet()) {
                    String value = bodyParams.get(name);
                    builder.addFormDataPart(name, value);
                }
            }
            for (String name : files.keySet()) {
                FilePara file = files.get(name);
                MediaType type = httpClient.getMediaType(file.type);
                RequestBody bodyPart;
                if (file.file != null) {
                    bodyPart = RequestBody.create(type, file.file);
                } else {
                    bodyPart = RequestBody.create(type, file.content);
                }
                builder.addFormDataPart(name, file.fileName, bodyPart);
            }
            return builder.build();
        } else if (requestJson != null) {
            return RequestBody.create(TYPE_JSON, requestJson);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            if (bodyParams != null) {
                for (String name : bodyParams.keySet()) {
                    String value = bodyParams.get(name);
                    builder.add(name, value);
                }
            }
            return builder.build();
        }
    }

    private String buildUrlPath() {
        String url = urlPath;
        if (url == null || url.trim().isEmpty()) {
            throw new HttpException("url 不能为空！");
        }
        if (pathParams != null) {
            for (String name : pathParams.keySet()) {
                String target = "{" + name + "}";
                if (url.contains(target)) {
                    url = url.replace(target, pathParams.get(name));
                } else {
                    throw new HttpException("pathParameter [ " + name + " ] 不存在于 url [ " + urlPath + " ]");
                }
            }
        }
        if (url.matches(PATH_PARAM_REGEX)) {
            throw new HttpException("url 里有 pathParameter 没有设置，你必须先调用 addPathParam 为其设置！");
        }
        if (urlParams != null) {
            url = buildUrl(url.trim());
        }
        return url;
    }

    private String buildUrl(String url) {
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            if (!url.endsWith("?")) {
                if (url.lastIndexOf("=") < url.lastIndexOf("?") + 2) {
                    throw new HttpException("url 格式错误，'？' 后没有发现 '='");
                }
                if (!url.endsWith("&")) {
                    sb.append('&');
                }
            }
        } else {
            sb.append('?');
        }
        for (String name : urlParams.keySet()) {
            sb.append(name).append('=').append(urlParams.get(name)).append('&');
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    protected void assertNotConflict(boolean isGetRequest) {
        if (isGetRequest) {
            if (requestJson != null) {
                throw new HttpException("GET 请求 不能调用 setRequestJson 方法！");
            }
            if (jsonParams != null) {
                throw new HttpException("GET 请求 不能调用 addJsonParam 方法！");
            }
            if (bodyParams != null) {
                throw new HttpException("GET 请求 不能调用 addBodyParam 方法！");
            }
            if (files != null) {
                throw new HttpException("GET 请求 不能调用 addFileParam 方法！");
            }
        }
        if (requestJson != null) {
            if (jsonParams != null) {
                throw new HttpException("方法 addJsonParam 与 setRequestJson 不能同时调用！");
            }
            if (bodyParams != null) {
                throw new HttpException("方法 addBodyParam 与 setRequestJson 不能同时调用！");
            }
            if (files != null) {
                throw new HttpException("方法 addFileParam 与 setRequestJson 不能同时调用！");
            }
        }
        if (jsonParams != null) {
            if (bodyParams != null) {
                throw new HttpException("方法 addBodyParam 与 addJsonParam 不能同时调用！");
            }
            if (files != null) {
                throw new HttpException("方法 addFileParam 与 addJsonParam 不能同时调用！");
            }
        }
    }

    /**
     * @param latch CountDownLatch
     * @return 是否未超时：false 表示已超时
     */
    protected boolean timeoutAwait(CountDownLatch latch) {
        try {
            return latch.await(httpClient.totalTimeoutMillis() * 10,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new HttpException("超时", e);
        }
    }

	protected HttpResult timeoutResult() {
		if (nothrow) {
			return new RealHttpResult(this, State.TIMEOUT);
		}
		throw new HttpException(State.TIMEOUT, "执行超时");
	}

}
