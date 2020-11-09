# 常见问题

## OkHttps 支持 SSL（HTTPS）吗？需要额外配置吗？

答：**支持，并不需要额外配置**，比如以下请求百度的网址，不需要任何配置就可以正常运行：

```java
HTTP http = HTTP.builder().build();
String baidu = http.sync("https://www.baidu.com")
    .get()
    .getBody()
    .toString();
System.out.println(baidu);
```

当然这有一个前提就是是服务器配置的 SSL 证书是值得信任并且有效的，这也是我们推荐的一种方式。

如果服务器的 SSL 证书不是在权威机构购买而是自己生成的（不推荐这种做法），则需要配置`sslSocketFactory`和`hostnameVerifier`即可：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.sslSocketFactory(mySSLSocketFactory, myTrustManager);
        b.hostnameVerifier(myHostnameVerifier);
    })
    .build();
```

例如，让 OkHttps 信任所有，上述代码中的`mySSLSocketFactory`、`myTrustManager`和`myHostnameVerifier`可通过如下方式生成:

```java
X509TrustManager myTrustManager = new X509TrustManager() {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
};

HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
};

SSLContext sslCtx = SSLContext.getInstance("TLS");
sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());

SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();
```

## OkHttps 支持 Cookie 吗？要怎么配置？

答：**支持**，配置方和 OkHttp 完全一样，只需要配置一个 CookieJar 即可：

```java
CookieJar myCookieJar = new CookieJar() {

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // TODO: 保存 cookies
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // TODO: 读取 cookies
        return null;
    }

};

HTTP http = HTTP.builder()
    .config(b -> {
        b.cookieJar(myCookieJar);
    })
    .build();
```

## 有失败重试机制吗？要怎么配置？

答：很简单，比如以下配置就可实现请求超时重试：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.addInterceptor(chain -> {
            int retryTimes = 0;	
            while (true) {
                try {
                    return chain.proceed(chain.request());
                } catch (SocketTimeoutException e) {
                    if (retryTimes >= 3) {
                        throw e;
                    }
                    retryTimes++;
                    System.out.println("超时重试第" + retryTimes + "次！");
                }
            }
        });
    }).build();
```

以下代码实现服务器状态码为 500 时，自动重试：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.addInterceptor(chain -> {
            int retryTimes = 0;	
            while (true) {
                Response response = chain.proceed(chain.request());
                if (response.code() == 500 && retryTimes < 3) {
                    retryTimes++;
                    System.out.println("失败重试第" + retryTimes + "次！");
                }
                return response;
            }
        });
    }).build();
```

当然也可以把两者结合起来：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.addInterceptor(chain -> {
            int retryTimes = 0;	
            while (true) {
                Response response = null;
                Exception exception = null;
                try {
                    response = chain.proceed(chain.request());
                } catch (Exception e) {
                    exception = e;
                }
                if ((exception != null || response.code() == 500) && retryTimes < 3) {
                    retryTimes++;
                    System.out.println("失败重试第" + retryTimes + "次！");
                } else if (exception != null) {
                    throw exception;
                }
                return response;
            }
        });
    }).build();
```

## HttpException：没有匹配[null]类型的转换器！

当出现这个异常时，一般是让 OkHttps 去自动解析 JSON 却没有给它配置`MsgConvertor`导致的，当遇到这个异常，可按如下步骤检查：

**1、** 项目依赖中是否添加了 json 扩展包：`okhttps-fastjson`、`okhttps-gson`、`okhttps-jackson`，添加一个即可；

**2、** 发起请求时，使用的是 OkHttps 提供的工具类（`OkHttps`或`HttpUtils`）还是 自己构建的`HTTP`实例，如果是前者，框架会自动配置`MsgConvertor`，若是后者，得自己手动配置`MsgConvertor`：

```java
HTTP http = HTTP.builder()
    .addMsgConvertor(new FastjsonMsgConvertor());   // okhttps-gson
    .addMsgConvertor(new GsonMsgConvertor());       // okhttps-fastjson
    .addMsgConvertor(new JacksonMsgConvertor());    // okhttps-jackson
    .build();
```

**3、** 项目依赖中已经添加了 json 扩展包，并且使用的是 OkHttps 提供的工具类（`OkHttps`或`HttpUtils`），但还是有这个异常（罕见），这个时候一般是 IDE 的编译器的 BUG 导致的，请 clean 一下项目，重新运行即可。

## JSON 请求后端收不到数据，JSON 被加上双引号当做字符串了？

```java
List<String> values = new ArrayList<>();
values.add("value1");
values.add("value2");

OkHttps.sync("/api/...")
    .bodyType(OkHttps.JSON)
    .addBodyPara("name", "Test")
    .addBodyPara("values", values)
    .post();
```

如上，用户可能期望发送这样的 JSON 给服务器：

```json
{
    "name": "Test",
    "values": [ "value1", "value2" ]
}
```

但实际上服务器收到的却是这样：

```json
{
    "name": "Test",
    "values": "[\"value1\", \"value2\"]"
}
```

这是因为`addBodyPara`方法添加的参数**只支持单层数据结构**，若要支持多层数据结构，必须使用`setBodyPara`方法，如下：

```java
List<String> values = new ArrayList<>();
values.add("value1");
values.add("value2");

Map<String, Object> paraMap = new HashMap<>();
paraMap.put("name", "Test");
paraMap.put("values", values);

OkHttps.sync("/api/...")
    .bodyType(OkHttps.JSON)
    .setBodyPara(paraMap)
    .post();
```
