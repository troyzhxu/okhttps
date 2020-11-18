# 常见问题

## 支持 SSL（HTTPS）吗？

答：**支持**，比如以下请求百度的带 https 的网址，不需要任何配置就可以正常运行：

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

## 支持 Cookie 吗？

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

## 支持代理（Proxy）吗？

答：**支持**，只需配置 Proxy 即可，例如：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www.your-proxy.com", 8080)));
    })
    .build();
```

## 支持缓存（Cache）吗？

答：**支持**，只需配置 Cache 即可，例如：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.cache(new Cache("/path-to-cache", 10 * 1024 * 1024));
    })
    .build();
```

## 有失败重试机制吗？

答：很简单，比如以下配置就可实现请求超时重试三次：

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

以下代码实现服务器状态码为 500 时，自动重试三次：

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
                    continue;
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
                    continue;
                } else if (exception != null) {
                    throw exception;
                }
                return response;
            }
        });
    }).build();
```

## HttpException: 没有匹配[null/json]类型的转换器！

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

## HttpException: 转换失败 Caused by IOException: closed

当出现这个异常时，很可能是对报文体重复消费（多次调用 toXXX 方法）造成的，类似以下代码：

```java
Body body = OkHttps.sync("/api/users/1").get().getBody();

log.info("body = " + body);             // 这里隐式的调用了 body 的 toString 消费方法

User user = body.toBean(User.class);    // 这里又调用了一次 toBean，将会抛出异常
```

以上代码，由于多次调用报文体的消费方法，则会导致此异常，如果确实需要多次消费时，可以先使用`cache`方法，如下：

```java
Body body = OkHttps.sync("/api/users/1").get().getBody()
        .cache();                       // 先调用 cache 方法，就可以多次消费了

log.info("body = " + body);             // 这里隐式的调用了 body 的 toString 消费方法

User user = body.toBean(User.class);    // 又调用了一次 toBean，则不会再有问题
Mapper mapper = body.toMapper();        // 再调用一次，依然没问题
```

## HttpException: 报文体转换字符串出错 Caused by IOException: Content-Length (xxx) and stream length (0) disagree

当出现这个异常，同样很可能是多次消费报文体的问题（同上），再类似以下的代码：

```java
HttpResult.Body body1 = OkHttps.async("/api/...")
        .setOnResponse(res -> {
            HttpResult.Body body2 = res.getBody();
            String str2 = body2.toString();     // 这里消费了一次报文体
            // ...
        })
        .get()
        .getResult()
        .getBody();

String str1 = body1.toString();                 // 这里又消费了一次报文体
```

以上的代码，在第 4、11 行都消费了报文体，但是没有提前使用`cache()`方法，所以会报错，如下修改即可：

```java
HttpResult.Body body1 = OkHttps.async("/api/...")
        .setOnResponse(res -> {
            HttpResult.Body body2 = res.getBody()
                    .cache();                   // 使用 cache
            String str2 = body2.toString();
            // ...
        })
        .get()
        .getResult()
        .getBody()
        .cache();                               // 使用 cache

String str1 = body1.toString();
```

若使用的 OkHttps 版本是 v2.4.2 及以前版本，上面的代码得考虑线程安全问题，加一个锁即可：

```java
Object lock = new Object();

HttpResult res1 = OkHttps.async("/api/...")
        .setOnResponse(res2 -> {

            synchronized(lock) {
            	String str2 = res2.getBody().cache().toString();
            	// ...
            }

        })
        .get()
        .getResult()

synchronized(lock) {
	String str1 = res1.getBody().cache().toString();
	// ...
}
```

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

## 还有其它问题，怎么解决？

1. 到 GitHub 的 issue 里看看有没有人提过类似的问题：[https://github.com/ejlchina/okhttps/issues?q=is%3Aissue+is%3Aclosed](https://github.com/ejlchina/okhttps/issues?q=is%3Aissue+is%3Aclosed)

2. 到 Gitee 的 issue 里看看有没有人提过类似的问题：[https://gitee.com/ejlchina-zhxu/okhttps/issues?assignee_id=&author_id=&branch=&issue_search=&label_name=&milestone_id=&program_id=&scope=&sort=&state=closed](https://gitee.com/ejlchina-zhxu/okhttps/issues?assignee_id=&author_id=&branch=&issue_search=&label_name=&milestone_id=&program_id=&scope=&sort=&state=closed)

3. 若问题还没得到解决，可先加微信：18556739726（请备注 OkHttps）再入群交流讨论。
