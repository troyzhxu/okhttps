package com.ejlchina.okhttps;

import com.ejlchina.okhttps.HttpResult.State;
import com.ejlchina.okhttps.internal.*;
import com.ejlchina.okhttps.internal.AbstractHttpClient.TagTask;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by 周旭（Troy.Zhou） on 2020/3/11.
 */
@SuppressWarnings("unchecked")
public abstract class HttpTask<C extends HttpTask<?>> implements Cancelable {

    private static final String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";
    private static final String DOT = ".";

    protected AbstractHttpClient httpClient;
    protected boolean nothrow;
    protected boolean nextOnIO = false;
    
    private final String urlPath;
    private String tag;
    private Map<String, String> headers;
    private Map<String, Object> pathParams;
    private Map<String, Object> urlParams;
    private Map<String, Object> bodyParams;
    private Map<String, FilePara> files;
    private Object requestBody;
    private String bodyType;
    private OnCallback<Process> onProcess;
    private boolean processOnIO;
    private long stepBytes = 0;
    private double stepRate = -1;

    private Object object;
    
    private TagTask tagTask;
    private Cancelable canceler;
    private Charset charset;

    protected boolean skipPreproc = false;
    protected boolean skipSerialPreproc = false;


    public HttpTask(AbstractHttpClient httpClient, String urlPath) {
        this.httpClient = httpClient;
        this.charset = httpClient.charset();
        this.bodyType = httpClient.bodyType();
        this.urlPath = urlPath;
    }

    /**
     * 获取请求任务的URL地址
     * @return URL地址
     */
    public String getUrl() {
        return urlPath;
    }

    /**
     * @return 是否是 Websocket 通讯
     */
    public boolean isWebsocket() {
        return false;
    }

    /**
     * @since 2.2.0
     * @return 是否是 同步 Http 请求
     */
    public boolean isSyncHttp() {
        return false;
    }

    /**
     * @since 2.2.0
     * @return 是否是 异步 Http 请求
     */
    public boolean isAsyncHttp() {
        return false;
    }

    /**
     * 获取请求任务的标签
     * @return 标签
     */
    public String getTag() {
        return tag;
    }

    public String getBodyType() {
        return bodyType;
    }

    /**
     * 标签匹配
     * 判断任务标签与指定的标签是否匹配（包含指定的标签）
     * @param tag 标签
     * @return 是否匹配
     */
    public boolean isTagged(String tag) {
        String theTag = this.tag;
        if (theTag != null && tag != null) {
            return theTag.equals(tag) || theTag.startsWith(tag + DOT) || theTag.endsWith(DOT + tag)
                || theTag.contains(DOT + tag + DOT);
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
     * @since 2.4.0
     * @return 路径参数
     */
    public Map<String, Object> getPathParas() {
        return pathParams;
    }

    /**
     * @since 2.4.0
     * @return URL参数（查询参数）
     */
    public Map<String, Object> getUrlParas() {
        return urlParams;
    }

    /**
     * @since 2.4.0
     * @return 报文体参数
     */
    public Map<String, Object> getBodyParas() {
        return bodyParams;
    }

    /**
     * @since 2.4.0
     * @return 文件参数
     */
    public Map<String, FilePara> getFileParas() {
        return files;
    }

    /**
     * @since 2.4.0
     * @return 报文体
     */
    public Object getRequestBody() {
        return requestBody;
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
     * 指定该请求跳过任何预处理器（包括串行和并行）
     * @return HttpTask 实例
     */
    public C skipPreproc() {
		this.skipPreproc = true;
		return (C) this;
	}

    /**
     * 指定该请求跳过任何串行预处理器
     * @return HttpTask 实例
     */
	public C skipSerialPreproc() {
		this.skipSerialPreproc = true;
		return (C) this;
	}

    /**
     * @since 2.0.0.RC
     * 为请求任务添加标签
     * @param tag 标签
     * @return HttpTask 实例
     */
    public C tag(String tag) {
        if (tag != null) {
            if (this.tag != null) {
                this.tag = this.tag + DOT + tag;
            } else {
                this.tag = tag;
            }
            updateTagTask();
        }
        return (C) this;
    }

    /**
     * @since 2.0.0
     * 设置该请求的编码格式
     * @param charset 编码格式
     * @return HttpTask 实例
     */
    public C charset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
        return (C) this;
    }

    /**
     * @since 2.0.0
     * 设置请求体的类型，如：form、json、xml、protobuf 等
     * @param type 请求类型
     * @return HttpTask 实例
     */
    public C bodyType(String type) {
        if (type != null) {
            this.bodyType = type.toLowerCase();
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
        processOnIO = nextOnIO;
        nextOnIO = false;
        return (C) this;
    }

    /**
     * 设置进度回调的步进字节，默认 8K（8192）
     * 表示每接收 stepBytes 个字节，执行一次进度回调
     * @param stepBytes 步进字节
     * @return HttpTask 实例
     */
    public C stepBytes(long stepBytes) {
        this.stepBytes = stepBytes;
        return (C) this;
    }

    /**
     * 设置进度回调的步进比例
     * 表示每接收 stepRate 比例，执行一次进度回调
     * @param stepRate 步进比例
     * @return HttpTask 实例
     */
    public C stepRate(double stepRate) {
        this.stepRate = stepRate;
        return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addPathPara(String name, Object value) {
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
    public C addPathPara(Map<String, ?> params) {
        if (pathParams == null) {
            pathParams = new HashMap<>();
        }
        if (params != null) {
            pathParams.putAll(params);
        }
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addUrlPara(String name, Object value) {
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
    public C addUrlPara(Map<String, ?> params) {
        if (urlParams == null) {
            urlParams = new HashMap<>();
        }
        if (params != null) {
            urlParams.putAll(params);
        }
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addBodyPara(String name, Object value) {
        if (name != null && value != null) {
            if (bodyParams == null) {
                bodyParams = new HashMap<>();
            }
            bodyParams.put(name, value);
        }
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addBodyPara(Map<String, ?> params) {
        if (bodyParams == null) {
            bodyParams = new HashMap<>();
        }
        if (params != null) {
            bodyParams.putAll(params);
        }
        return (C) this;
    }

    /**
     * 设置 请求报文体
     * @param body 请求体，字节数组、字符串 或 Java对象（由 MsgConvertor 来序列化）
     * @return HttpTask 实例
     **/
    public C setBodyPara(Object body) {
        this.requestBody = body;
        return (C) this;
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param filePath 文件路径
     * @return HttpTask 实例
     */
    public C addFilePara(String name, String filePath) {
        return addFilePara(name, new File(filePath));
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param type 文件类型/扩展名: 如 txt、png、jpg、doc 等，参考 @{ HTTP$Builder#mediaTypes }
     * @param filePath 文件路径
     * @return HttpTask 实例
     */
    public C addFilePara(String name, String type, String filePath) {
        return addFilePara(name, type, new File(filePath));
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param file 文件
     * @return HttpTask 实例
     */
    public C addFilePara(String name, File file) {
        if (file != null && file.exists()) {
            String fileName = file.getName();
            String type = fileName.substring(fileName.lastIndexOf(DOT) + 1);
            return addFilePara(name, type, file);
        }
        return (C) this;
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param type 文件类型/扩展名: 如 txt、png、jpg、doc 等
     * @param file 文件
     * @return HttpTask 实例
     */
    public C addFilePara(String name, String type, File file) {
        if (name != null && file != null && file.exists()) {
            if (files == null) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, file.getName(), file));
        }
        return (C) this;
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param type 文件类型/扩展名: 如 txt、png、jpg、doc 等
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFilePara(String name, String type, byte[] content) {
        return addFilePara(name, type, name + DOT + type, content);
    }

    /**
     * 添加文件参数（以 multipart/form-data 形式上传）
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFilePara(String name, String type, String fileName, byte[] content) {
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

    protected void registeTagTask(Cancelable canceler) {
        if (tag != null && tagTask == null) {
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
        Request request = prepareRequest(method.toUpperCase());
		return httpClient.request(request);
    }

    protected Request prepareRequest(String method) {
        boolean bodyCanUsed = HttpMethod.permitsRequestBody(method);
        assertNotConflict(!bodyCanUsed);
		Request.Builder builder = new Request.Builder()
                .url(buildUrlPath());
        buildHeaders(builder);
        if (bodyCanUsed) {
            RequestBody reqBody = buildRequestBody();
            if (onProcess != null) {
                long contentLength = contentLength(reqBody);
                if (stepRate > 0 && stepRate <= 1) {
                    stepBytes = (long) (contentLength * stepRate);
                }
                if (stepBytes <= 0) {
                    stepBytes = Process.DEFAULT_STEP_BYTES;
                }
                reqBody = new ProcessRequestBody(reqBody, onProcess,
                        httpClient.executor().getExecutor(processOnIO),
                        contentLength, stepBytes);
            } else {
                reqBody = new FixedRequestBody(reqBody);
            }
            builder.method(method, reqBody);
        } else {
            builder.method(method, null);
        }
        if (tag != null) {
            builder.tag(String.class, tag);
        }
		return builder.build();
	}

    private long contentLength(RequestBody reqBody) {
        try {
            return reqBody.contentLength();
        } catch (IOException e) {
            throw new OkHttpsException("无法获取请求体长度", e);
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

    protected State toState(IOException e) {
        if (e instanceof SocketTimeoutException) {
            return State.TIMEOUT;
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) {
            return State.NETWORK_ERROR;
        }
        String msg = e.getMessage();
        if (msg != null && ("Canceled".equals(msg) || e instanceof SocketException
                && (msg.startsWith("Socket operation on nonsocket") || "Socket closed".equals(msg)))) {
            return State.CANCELED;
        }
        return State.EXCEPTION;
    }

    private RequestBody buildRequestBody() {
        if (bodyParams != null && OkHttps.FORM_DATA.equalsIgnoreCase(bodyType)
                || files != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (bodyParams != null) {
                bodyParams.forEach((key, value) -> {
                    if (value == null) return;
                    byte[] content = value.toString().getBytes(charset);
                    RequestBody body = RequestBody.create(null, content);
                    builder.addPart(MultipartBody.Part.createFormData(key, null, body));
                });
            }
            if (files != null) {
                for (String name : files.keySet()) {
                    FilePara file = files.get(name);
                    MediaType type = httpClient.mediaType(file.type);
                    RequestBody bodyPart;
                    if (file.file != null) {
                        bodyPart = RequestBody.create(type, file.file);
                    } else {
                        bodyPart = RequestBody.create(type, file.content);
                    }
                    builder.addFormDataPart(name, file.fileName, bodyPart);
                }
            }
            return builder.build();
        }
        if (requestBody != null) {
            return toRequestBody(requestBody);
        }
        if (bodyParams == null) {
            return emptyRequestBody();
        }
        if (OkHttps.FORM.equalsIgnoreCase(bodyType)) {
            FormBody.Builder builder = new FormBody.Builder(charset);
            bodyParams.forEach((key, value) -> {
                if (value == null) return;
                builder.add(key, value.toString());
            });
            return builder.build();
        }
        return toRequestBody(bodyParams);
    }

    private RequestBody emptyRequestBody() {
        if (OkHttps.FORM_DATA.equalsIgnoreCase(bodyType)) {
            return new MultipartBody.Builder().setType(MultipartBody.FORM).build();
        }
        return RequestBody.create(mediaType(), new byte[]{});
    }

    private MediaType mediaType() {
        return httpClient.executor().doMsgConvert(bodyType, null).mediaType(charset);
    }

    private RequestBody toRequestBody(Object object) {
        if (object instanceof byte[] || object instanceof String) {
            byte[] body = object instanceof byte[] ? (byte[]) object : ((String) object).getBytes(charset);
            return RequestBody.create(mediaType(), body);
        }
        TaskExecutor.Data<byte[]> data = httpClient.executor().doMsgConvert(bodyType, c -> c.serialize(object, charset));
        return RequestBody.create(data.mediaType(charset), data.data);
    }

    private String buildUrlPath() {
        String url = urlPath;
        if (url == null || url.trim().isEmpty()) {
            throw new OkHttpsException("url 不能为空！");
        }
        if (pathParams != null) {
            for (String name : pathParams.keySet()) {
                String target = "{" + name + "}";
                if (url.contains(target)) {
                    Object value = pathParams.get(name);
                    url = url.replace(target, value != null ? value.toString() : "");
                } else {
                    throw new OkHttpsException("pathPara [ " + name + " ] 不存在于 url [ " + urlPath + " ]");
                }
            }
        }
        if (url.matches(PATH_PARAM_REGEX)) {
            throw new OkHttpsException("url 里有 pathPara 没有设置，你必须先调用 addPathPara 为其设置！");
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
                    throw new OkHttpsException("url 格式错误，'?' 后没有发现 '='");
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

    /**
     * 参数冲突校验
     */
    protected void assertNotConflict(boolean bodyCantUsed) {
        if (bodyCantUsed) {
            if (requestBody != null) {
                throw new OkHttpsException("GET | HEAD 请求 不能调用 setBodyPara 方法！");
            }
            if (isNotEmpty(bodyParams)) {
                throw new OkHttpsException("GET | HEAD 请求 不能调用 addBodyPara 方法！");
            }
            if (isNotEmpty(files)) {
                throw new OkHttpsException("GET | HEAD 请求 不能调用 addFilePara 方法！");
            }
        }
        if (requestBody != null) {
            if (isNotEmpty(bodyParams)) {
                throw new OkHttpsException("方法 addBodyPara 与 setBodyPara 不能同时使用！");
            }
            if (isNotEmpty(files)) {
                throw new OkHttpsException("方法 addFilePara 与 setBodyPara 不能同时使用！");
            }
        }
    }

    private static boolean isNotEmpty(Map<String, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * @param latch CountDownLatch
     * @return 是否未超时：false 表示已超时
     */
    protected boolean timeoutAwait(CountDownLatch latch) {
        try {
            return latch.await(httpClient.preprocTimeoutMillis(),
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new OkHttpsException("执行超时: " + urlPath, e);
        }
    }

    protected HttpResult timeoutResult() {
        if (nothrow) {
            return new RealHttpResult(this, State.TIMEOUT);
        }
        throw new OkHttpsException(State.TIMEOUT, "执行超时: " + urlPath);
    }

    public Charset charset(Response response) {
        ResponseBody b = response.body();
        MediaType type = b != null ? b.contentType() : null;
        return type != null ? type.charset(charset) : charset;
    }

    protected void execute(Runnable command, boolean onIo) {
        httpClient.executor().execute(command, onIo);
    }

}
