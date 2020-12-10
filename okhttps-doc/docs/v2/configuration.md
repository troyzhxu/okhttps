---
description: OkHttps 配置 BaseUrl 回调执行器 主线程 UI线程 OkHttpClient 预处理器 TOKEN问题最佳解决方案 刷新TOKEN 全局 回调监听 下载监听 回调阻断 拦截器 CookieJar、SSL、缓存、代理、事件监听
---

# 配置

## 设置 BaseUrl

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")    // 设置 BaseUrl
        .build();
```
　　该配置全局生效，在配置了`BaseUrl`之后，具体的请求便可以省略`BaseUrl`部分，使得代码更加简洁，例如：

```java
http.sync("/users").get()                  // http://api.demo.com/users

http.sync("/auth/signin")                  // http://api.demo.com/auth/signin
        .addBodyPara("username", "Jackson")
        .addBodyPara("password", "xxxxxx")
        .post();                           // POST请求
```
　　在配置了`BaseUrl`之后，如有特殊请求任务，仍然可以使用全路径的方式，一点都不妨碍：

```java
http.sync("https://www.baidu.com").get();
```

::: tip 支持动态 BaseUrl 吗？
有些同学可能在网上看到某些网络封装框架声明支持 **动态 BaseUrl** 或者说 **动态域名**，那 OkHttps 支持吗？

答：OkHttps 是 **不会** 提供在同一个`HTTP`实例上可以动态设置`BaseUrl`的功能的。

为什么呢？

答：因为这完全是一种缺陷设计，我们都知道在 Java 中多线程开发很常见，如果 A 线程正兴高采烈的准备向某个地址发起请求时，而 B 线程突然修改了`BaseUrl`，那 A 线程不就悲剧了么。所以类似 Retrofit 这些设计成熟的框架都不会提供这种功能的。

那我的应用中确实要用到多个域名，该怎么办呢？

答：可以构建多个`HTTP`实例，不同的实例负责不同的域名，如果每个域名的请求都比较少，就是域名多，那便只用一个`HTTP`实例，请求较少的域名使用全路径访问即可。
:::

## 回调执行器

　　OkHttps 默认所有回调都在 **IO线程** 执行，如何想改变执行回调的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在 UI 线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置默认 [**影响所有回调**](/v2/foundation.html#回调函数)，另 [**全局监听**](/v2/configuration.html#全局监听) 不受此影响，更多实现细节可参考 [安卓-回调线程切换](/v2/android.html#回调线程切换) 章节。

::: warning 注意
在 Android 7+（SDK 24 以上）中使用 v2.0.0 及以前版本，当在主线程里消费报文体时（调用`Body#toXxx()`方法），会引发`android.os.NetworkOnMainThreadException`异常。可以通过添加一个拦截器来解决：

```java
HTTP http = HTTP.builder()
        .config( builder -> builder.addInterceptor(chain -> {
            Response res = chain.proceed(chain.request());
            ResponseBody body = res.body();
            ResponseBody newBody = null;
            if (body != null) {
                newBody = ResponseBody.create(body.contentType(), body.bytes());
            }
            return res.newBuilder().body(newBody).build();
        }))
        // 省略其它...
        .build();
```
:::

## 预处理器

预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与`OkHttp`的拦截器（`Interceptor`）不同：预处理器可以让我们 **异步** 处理这些问题。

在预处理器中，我们通常可以：

1. 统一（或根据标签条件）添加请求头（可同步或异步执行）
2. 统一（或根据标签条件）添加请求参数（可同步或异步执行）
3. 其它自定义的管理（比如 请求日志、[生命周期绑定](/v2/android.html#生命周期绑定) 等）

### 并行预处理器

例如，当我们想为请求任务自动添加`Token`头信息，而`Token`只能通过异步方法`requestToken`获取时，这时使用`Interceptor`就很难处理了，但可以使用预处理器轻松解决：

```java
HTTP http = HTTP.builder()
        .addPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();// 获得当前的HTTP任务
            if (!task.isTagged("Auth")) {      // 根据标签判断该任务是否需要Token
                chain.proceed();
                return;
            }
            requestToken((String token) -> {   // 异步获取 Token
                task.addHeader("Token", token);// 为任务添加头信息
                chain.proceed();               // 继续当前的任务
            });
        })
        .build();
```

和`Interceptor`一样，`Preprocessor`也可以添加多个。他们之间的区别如下:

::: tip 拦截器与预处理器的区别
* 拦截器只能处理同步操作，预处理器支持处理异步操作
* 拦截器都是并行处理请求，预处理器支持串行处理（详见6.5章节）
* 拦截器处理时机在请求前和响应后，预处理器只在请求前，并且先于拦截器执行。关于响应后，OkHttps还提供了全局回调监听（详见6.6章节）
:::

并行预处理器还可以实现更多功能，比如 [安卓-生命周期绑定](/v2/android.html#生命周期绑定)、[安卓-自动加载框](/v2/android.html#自动加载框) 等。

### 串行预处理器（TOKEN问题最佳解决方案）

　　普通预处理器都是可并行处理的，然而有时我们希望某个预处理器同时只处理一个任务。比如 当`Token`过期时我们需要去刷新获取新`Token`，而刷新`Token`这个操作只能有一个任务去执行，因为如果`n`个任务同时执行的话，那么必有`n-1`个任务刚刷新得到的`Token`可能就立马失效了，而这是我们所不希望的。

　　为了解决这个问题，OkHttps 提供了串行预处理器，它可以让 HTTP 任务排好队，一个一个地进入预处理器：

```java
HTTP http = HTTP.builder()
        .addSerialPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();
            if (!task.isTagged("Auth")) {
                chain.proceed();
                return;
            }
            // 检查过期，若需要则刷新Token
            requestTokenAndRefreshIfExpired((String token) -> {
                task.addHeader("Token", token);            
                chain.proceed();    // 调用此方法前，不会有其它任务进入该处理器
            });
        })
        .build();
```

串行预处理器实现了让HTTP任务排队串行处理的功能，但值得一提的是：它并没有因此而阻塞任何线程！

::: warning 注意
由于 串行预处理器 只能一个一个的处理任务，所以在上述中的`requestTokenAndRefreshIfExpired`方法里，若发起网络请求，一定要跳过串行预处理器 [可使用`skipSerialPreproc()`方法]，例如：

```java
http.async("/oauth/refresh-token")
        .skipSerialPreproc()    // 跳过串行预处理器
        .addBodyPara("refreshToken", "xxxxxx")
        .setOnResponse((HttpResult result) -> {

        })
        .post();
```
否则就会发生两个 HTTP 任务相互等待谁也执行不了的问题。

如果你使用的是 v1.x 的版本，则可以使用`HTTP`实例的`request(Request request)`方法发起原生请求，这样也不经过任何预处理器。
:::

关于 TOKEN 的更多处理细节，请参考 [安卓-最佳实践](/v2/android.html#最佳实践) 章节。

## 消息转换器

OkHttps 自 v2.0 后开始支持自定义消息转换器，并且可以添加多个，例如：

```java
HTTP http = HTTP.builder()
        .addMsgConvertor(new MyJsonMsgConvertor());
        .addMsgConvertor(new MyXmlMsgConvertor());
        .build();
```

配置了消息转换器后，`HTTP`实例便具有了序列化和反序列化这些格式数据的能力。

* `okhttps-gson`里提供了`GsonMsgConvertor`
* `okhttps-jackson`里提供了`JacksonMsgConvertor`
* `okhttps-fastjson`里提供了`FastjsonMsgConvertor`
* `okhttps-xml`里提供了`XmlMsgConvertor`

### 反序列化

例如下例中，无论该接口响应的是 JSON 还是 XML，都可以反序列化成功：

```java
// 无论该接口响应的是 JSON 还是 XML，都可以反序列化成功
List<Order> orders = http.sync('/orders')
        .get().getBody().toList(Order.class);
```

在 Websocket 通讯中，也是如此：

```java
http.webSocket("/redpacket/status") // 监听红包的领取状态
        .setOnMessage((WebSocket ws，Message msg) -> {
            // 无论该接口响应的是 JSON 还是 XML，都可以反序列化成功
            Status status = msg.toBean(Status.class);
        })
        .listen();
```

### 正序列化

另外，如果要在请求发出阶段正向序列化参数，则需要指定`bodyType`参数告诉 OkHttps 你想使用哪一个消息转换器，如：

```java
Order order = createOrder();
http.async('/orders')           // 提交订单
        .bodyType("json")
        .setBodyPara(order)     // 以 JSON 格式序列化 Order 对象
        .post();
```

或者 XML：

```java
Order order = createOrder();
http.async('/orders')           // 提交订单
        .bodyType("xml")
        .setBodyPara(order)     // 以 XML 格式序列化 Order 对象
        .post();
```

在 Websocket 里，也是同样的方法：

```java
http.webSocket("/chat") 
        .bodyType("json")
        .setOnOpen((WebSocket ws，HttpResult res) -> {
            Hello hello = getHello();
            ws.send(hello);     // 以 JSON 格式序列化 Hello 对象
        })
        .listen();
```

但如果你在 Websocket 通讯到某一个阶段后，突然想换另外一种格式来发送数据了，你还可以这样：

```java
http.webSocket("/chat") 
        .bodyType("json")
        .setOnOpen((WebSocket ws，HttpResult res) -> {
            Hello hello = getHello();
            ws.send(hello);     // 以 JSON 格式序列化 Hello 对象

            ws.msgType("xml")   // 切换为 XML 格式
            ws.send(hello);     // 以 XML 格式序列化 Hello 对象
        })
        .listen();
```

### 默认序列化类型

然而大多数情况下，我们都使用一种消息转换器，比如 json，这时候你可以为`bodyType`配置一个默认值：

```java
HTTP http = HTTP.builder()
        // 修改 bodyType 的默认值为 json，若不修改，则默认是 form
        .bodyType("json");      
        .addMsgConvertor(new MyJsonMsgConvertor());
        .build();
```
::: tip 提示
只有请求报文体的正向序列化才会收到`bodyType`的影响，对于响应报文体的反向序列化不受其影响。
:::

### 表单序列化

OkHttps 不但能做 JSON 或 XML 的序列化，并且还能做表单参数的序列化，并且自带了一个`FormConvertor`，它可以给任意`MsgConvertor`赋予表单序列化的能力，例如你已经实现了一个`MyJsonMsgConvertor`，可以这么做：

```java
// 这里并不要求是一个 JSON 转换器，XML 也可以
// 有啥就给啥，效果是一样的
MsgConvertor convertor = new MyJsonMsgConvertor();

HTTP http = HTTP.builder()
        .addMsgConvertor(new MsgConvertor.FormConvertor(convertor));
        .build();
```

这样，你再做表单请求时，就可以直接扔进一个对象，它将自动完成序列化的事情：

```java
Order order = createOrder();
http.async('/orders')           // 提交订单
        .bodyType("form")
        .setBodyPara(order)     // 以 表单 格式序列化 Order 对象
        .post();
```

一般情况下，如果你有实现了一个`MsgConvertor`，我们推荐把它和`FormConvertor`都添加进去，例如：

```java
MsgConvertor convertor = new MyJsonMsgConvertor();

HTTP http = HTTP.builder()
        .addMsgConvertor(convertor);
        .addMsgConvertor(new MsgConvertor.FormConvertor(convertor));
        .build();
```

如果你添加了官方提供的`MsgConvertor`依赖包（例如：`okhttps-jackson`等），在构建实例时，还可以这样直接注入：

```java
HTTP.Builder builder = HTTP.builder()
// 自动完成依赖中的 MsgConvertor 和 FormConvertor 的注入
ConvertProvider.inject(builder);
HTTP http = builder.build();
```

::: tip
如果你使用的是[`OkHttps`或`HttpUtils`工具类](/v2/getstart.html#工具类)，它们都会自动配置`MsgConvertor`和`FormConvertor`，无需手动配置

在 v2.0.0.RC 版本中`FormConvertor`的名字是`MsgConvertor.FormMsgConvertor`
:::

## 全局监听

### 全局回调监听

　　全局回调是实际开发中经常需要的功能，比如对服务器响应的状态码进行统一处理等，同时 OkHttps 的全局回调还具有 **回调阻断** 的功能：

```java
HTTP http = HTTP.builder()
        .responseListener((HttpTask<?> task, HttpResult result) -> {
            // 所有异步请求（包括 WebSocket）响应后都会走这里

            // 返回 true 表示继续执行 task 的 OnResponse 回调，
            // 返回 false 则表示不再执行，即 阻断
            return true; 
        })
        .completeListener((HttpTask<?> task, State state) -> {
            // 所有异步请求（包括 WebSocket）执行完都会走这里

            // 返回 true 表示继续执行 task 的 OnComplete 回调，
            // 返回 false 则表示不再执行，即 阻断
            return true;
        })
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有异步请求（包括 WebSocket）发生异常都会走这里

            // 返回 true 表示继续执行 task 的 OnException 回调，
            // 返回 false 则表示不再执行，即 阻断
            return true;
        })
        .build();
```

**回调阻断** 其实是日常开发中比较常见的需求，比如：

只有当接口响应的状态码在 [200, 300) 之间时，应用才做具体的业务处理，其它则一律向用户提示接口返回的错误信息，则可以这么做：

```java
HTTP http = HTTP.builder()
        .responseListener((HttpTask<?> task, HttpResult result) -> {
            if (result.isSuccessful()) {
                return true;            // 继续接口的业务处理
            }
            showApiMsgToUser(result);   // 向用户展示接口的错误信息
            return false;               // 阻断
        })
        .build();
```

然后具体的请求，就可以专注于业务处理，而不需担心接口的状态出错，比如：

```java
Order order = createOrder();   // 订单信息

http.async('/orders')       // 提交订单
        .setBodyPara(order)
        .setOnResponse((HttpResult result) -> {
            // 进入该回调，则表示订单已提交成功，就可以放心的做接下来的业务处理
            // 比如去发起支付：
            startPay(result.getBody().toBean(PayInfo.class));
        })
        .post();
```

::: tip 全局回调监听与拦截器的异同：
* 拦截器可以添加多个，全局回调监听分三种，每种最多添加一个
* 拦截器处的理时机在请求前和响应后，全局回调监听只在响应后，并且晚于拦截器
* 全局回调监听可以 **阻断**（return false）某个请求的具体回调，而拦截器不能
:::

::: warning
如果你开发的是安卓应用，我们强烈建议你添加 [全局异常监听](/v2/configuration.html#全局回调监听)，这样当你在某个请求中忘记使用`OnException`或`nothrow`，而它又发生了超时或网络异常时，不至于让程序崩溃。
:::

### 全局下载监听

```java
HTTP http = HTTP.builder()
        .downloadListener((HttpTask<?> task, Download download) -> {
            // 所有下载在开始之前都会先走这里
            Ctrl ctrl = download.getCtrl();         // 下载控制器
            
        })
        .build();
```

## 配置 OkHttpClient

　　与其他封装 OkHttp3 的框架不同，OkHttps 并不会遮蔽 OkHttp3 本身就很好用的功能，如下：

```java
HTTP http = HTTP.builder()
    .config((OkHttpClient.Builder builder) -> {
        // 配置连接池 最小10个连接（不配置默认为 5）
        builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        // 配置连接超时时间（默认10秒）
        builder.connectTimeout(20, TimeUnit.SECONDS);
        // 配置 WebSocket 心跳间隔（默认没有心跳）
        builder.pingInterval(60, TimeUnit.SECONDS);
        // 配置拦截器
        builder.addInterceptor((Chain chain) -> {
            Request request = chain.request();
            // 必须同步返回，拦截器内无法执行异步操作
            return chain.proceed(request);
        });
        // 其它配置: CookieJar、SSL、缓存、代理、事件监听...
        // 所有 OkHttp 能配置的，都可以在这里配置
    })
    .build();
```

<br/>

<Vssue :title="$title" />