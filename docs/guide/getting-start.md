
## 解析请求结果

### 回调函数

　　OkHttps 的回调函数全部使用单方法模式，这样可以充分利用 Java8 或 Kotlin 中的 Lambda 表达式，使你的代码更加简洁优雅：

```java
http.async("/users/{id}")             // http://api.demo.com/users/1
        .addPathParam("id", 1)
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        })
        .setOnException((IOException e) -> {
            // 异常回调
        })
        .setOnComplete((State state) -> {
            // 完成回调，无论成功失败都会执行
            // 并且在 响应|异常回调 之前执行
        })
        .get();
```

>* 只有异步请求才可以设置这三种（响应|异常|完成）回调
>* 同步请求直接返回结果，无需使用回调

### HttpResult

　　`HttpResult`是HTTP请求执行完后的结果，它是同步请求方法（ `get`、`post`、`put`、`delete`）的返回值，也是异步请求响应回调（`OnResponse`）的参数，它定义了如下方法：

* `getState()`         得到请求执行状态枚举，它有以下取值：
    * `State.CANCELED`      请求被取消
    * `State.RESPONSED`     已收到响应
    * `State.TIMEOUT`       请求超时
    * `State.NETWORK_ERROR` 网络错误
    * `State.EXCEPTION`     其它请求异常
* `getStatus()`        得到HTTP状态码
* `isSuccessful()`     是否响应成功，状态码在 [200..300) 之间
* `getHeaders()`       得到HTTP响应头
* `getHeaders(String name)` 得到HTTP响应头
* `getHeader(String name)`  得到HTTP响应头
* `getBody()`          得到响应报文体`Body`实例，它定义了如下方法（对同一个`Body`实例，以下的`toXXX()`类方法只能使用一个且仅能调用一次，除非先使用 cache 方法）：
    * `toBytes()`                     返回字节数组
    * `toByteStream()`                返回字节输入流
    * `toCharStream()`                返回字符输入流
    * `toString()`                    返回字符串
    * `toJsonObject()`                返回Json对象
    * `toJsonArray()`                 返回Json数组
    * `toBean(Class<T> type)`         返回根据type自动json解析后的JavaBean
    * `toList(Class<T> type)`         返回根据type自动json解析后的JavaBean列表
    * `toFile(String filePath)`       下载到指定路径
    * `toFile(File file)`             下载到指定文件
    * `toFolder(String dirPath)`      下载到指定目录
    * `toFolder(File dir)`            下载到指定目录
    * `getContentType()`              返回报文体的媒体类型
    * `getContentLength()`            返回报文体的字节长度
    * `cache()`                       缓存报文体，开启缓存后可重复使用`toXXX()`类方法
    * `close()`                       关闭报文体，未对报文体做任何消费时使用，比如只读取报文头
* `getError()`         执行中发生的异常，自动捕获执行请求时发生的 网络超时、网络错误 和 其它IO异常
* `close()`            关闭报文，未对报文体做任何消费时使用，比如只读取长度

　　示例，请求结果自动转Bean和List：

```java
// 自动转Bean
Order order = http.sync("/orders/1")
        .get().getBody().toBean(Order.class);
        
// 自动转List
List<Order> orders = http.sync("/orders")
        .get().getBody().toList(Order.class);
```

　　示例，使用 cache 方法：

```java
Body body = http.sync("/orders").get().getBody().cache();

// 使用 cache 后，可以多次使用 toXXX() 方法
System.out.println(body.toString());
System.out.println(body.toJsonArray());
System.out.println(body.toList(Order.class));
```

　　示例，获取下载文件的大小：

```java
long size = http.sync("/download/test.zip")
            .get().getBody()
            .close()             // 只是想获得文件大小，不消费报文体，所以直接关闭
            .getContentLength(); // 获得待下载文件的大小

// 由于未消费报文体，所以本次请求不会消耗下载报文体的时间和网络流量

System.out.println("size = " + size);
```


### HttpCall

　　`HttpCall`对象是异步请求方法（`get`、`post`、`put`、`delete`）的返回值，与`java`的`Future`接口很像，它有如下方法：

* `cancel()` 取消本次请求，返回取消结果
* `isCanceled()` 返回请求是否被取消
* `isDone()` 返回是否执行完成，包含取消和失败
* `getResult()` 返回执行结果`HttpResult`对象，若请求未执行完，则挂起当前线程直到执行完成再返回

　　取消一个异步请求示例：

```java
HttpCall call = http.async("/users/1").get();

System.out.println(call.isCanceled());     // false

boolean success = call.cancel();           // 取消请求

System.out.println(success);               // true
System.out.println(call.isCanceled());     // true
```

## 构建HTTP任务

　　`HTTP`对象的`sync`与`async`方法返回一个`HttpTask`对象，该对象提供了可链式调用的`addXXX`与`setXXX`等系列方法用于构建任务本身。

* `addHeader(String name, String value)`    添加请求头
* `addHeader(Map<String, String> headers)`  添加请求头

* `addPathParam(String name, Object value)` 添加路径参数：替换URL里的{name}占位符
* `addPathParam(Map<String, ?> params)`     添加路径参数：替换URL里的{name}占位符

* `addUrlParam(String name, Object value)`  添加URL参数：拼接在URL的?之后（查询参数）
* `addUrlParam(Map<String, ?> params)`      添加URL参数：拼接在URL的?之后（查询参数）

* `addBodyParam(String name, Object value)` 添加Body参数：以表单key=value&的形式放在报文体内（表单参数）
* `addBodyParam(Map<String, ?> params)`     添加Body参数：以表单key=value&的形式放在报文体内（表单参数）

* `addJsonParam(String name, Object value)` 添加Json参数：请求体为Json（支持多层结构）
* `addJsonParam(Map<String, ?> params)`     添加Json参数：请求体为Json（支持多层结构）

* `setRequestJson(Object json)`             设置请求体的Json字符串 或待转换为 Json的 JavaBean        
* `setRequestJson(Object bean, String dateFormat)` 设置请求体的Json字符串 或待转换为 Json的 JavaBean 

* `addFileParam(String name, String filePath)` 上传文件
* `addFileParam(String name, File file)` 上传文件
* `addFileParam(String name, String type, InputStream inputStream)` 上传文件
* `addFileParam(String name, String type, String fileName, InputStream input)` 上传文件
* `addFileParam(String name, String type, byte[] content)` 上传文件
* `addFileParam(String name, String type, String fileName, byte[] content)` 上传文件

* `setTag(String tag)` 为HTTP任务添加标签
* `setRange(long rangeStart)` 设置Range头信息，用于断点续传
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，可用于分块下载

* `bind(Object object)` 绑定一个对象，可用于实现Android里的生命周期绑定

## 使用标签

　　有时候我们想对HTTP任务加以分类，这时候可以使用标签功能：

```java
http.async("/users")    //（1）
        .setTag("A")
        .get();
        
http.async("/users")    //（2）
        .setTag("A.B")
        .get();
        
http.async("/users")    //（3）
        .setTag("B")
        .get();
        
http.async("/users")    //（4）
        .setTag("B")
        .setTag("C")    // 从 v1.0.4 标签将以追加模式添加，等效于 setTag("B.C")
        .get();
        
http.async("/users")    //（5）
        .setTag("C")
        .get();
```
　　当使用标签后，就可以按标签批量的对HTTP任务进行取消：

```java
int count = http.cancel("B");              //（2）（3）（4）被取消（取消标签包含"B"的任务）
System.out.println(count);                 // 输出 3
```
　　标签除了可以用来取消任务，在预处理器中它也可以发挥作用，请参见[并行预处理器](#54-并行预处理器)与[串行预处理器](#55-串行预处理器)。

## 配置 HTTP

### 设置 BaseUrl

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")    // 设置 BaseUrl
        .build();
```
　　该配置全局生效，在配置了`BaseUrl`之后，具体的请求便可以省略`BaseUrl`部分，使得代码更加简洁，例如：

```java
http.sync("/users").get()                  // http://api.demo.com/users

http.sync("/auth/signin")                  // http://api.demo.com/auth/signin
        .addBodyParam("username", "Jackson")
        .addBodyParam("password", "xxxxxx")
        .post()                            // POST请求
```
　　在配置了`BaseUrl`之后，如有特殊请求任务，仍然可以使用全路径的方式，一点都不妨碍：

```java
http.sync("https://www.baidu.com").get()
```

### 回调执行器

　　OkHttps 默认所有回调都在 **IO线程** 执行，如何想改变执行回调的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在UI线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置默认 **影响所有回调**。

### 配置 OkHttpClient

　　与其他封装 OkHttp3 的框架不同，OkHttps 并不会遮蔽 OkHttp3 本身就很好用的功能，如下：

```java
HTTP http = HTTP.builder()
    .config((Builder builder) -> {
        // 配置连接池 最小10个连接（不配置默认为 5）
        builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        // 配置连接超时时间（默认10秒）
        builder.connectTimeout(20, TimeUnit.SECONDS);
        // 配置拦截器
        builder.addInterceptor((Chain chain) -> {
            Request request = chain.request();
            // 必须同步返回，拦截器内无法执行异步操作
            return chain.proceed(request);
        });
        // 其它配置: CookieJar、SSL、缓存、代理、事件监听...
    })
    .build();
```

### 并行预处理器

　　预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与`OkHttp`的拦截器（`Interceptor`）不同：预处理器可以让我们 **异步** 处理这些问题。

　　例如，当我们想为请求任务自动添加`Token`头信息，而`Token`只能通过异步方法`requestToken`获取时，这时使用`Interceptor`就很难处理了，但可以使用预处理器轻松解决：

```java
HTTP http = HTTP.builder()
        .addPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();// 获得当前的HTTP任务
            if (!task.isTagged("Auth")) {      // 根据标签判断该任务是否需要Token
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

> * 拦截器只能处理同步操作，预处理器支持处理异步操作
> * 拦截器都是并行处理请求，预处理器支持串行处理（详见6.5章节）
> * 拦截器处理时机在请求前和响应后，预处理器只在请求前，并且先于拦截器执行。关于响应后，OkHttps还提供了全局回调监听（详见6.6章节）

### 串行预处理器（TOKEN问题最佳解决方案）

　　普通预处理器都是可并行处理的，然而有时我们希望某个预处理器同时只处理一个任务。比如 当`Token`过期时我们需要去刷新获取新`Token`，而刷新`Token`这个操作只能有一个任务去执行，因为如果`n`个任务同时执行的话，那么必有`n-1`个任务刚刷新得到的`Token`可能就立马失效了，而这是我们所不希望的。

　　为了解决这个问题，OkHttps 提供了串行预处理器，它可以让 HTTP 任务排好队，一个一个地进入预处理器：

```java
HTTP http = HTTP.builder()
        .addSerialPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();
            if (!task.isTagged("Auth")) {
                return;
            }
            // 检查过期，若需要则刷新Token
            requestTokenAndRefreshIfExpired((String token) -> {
                task.addHeader("Token", token);            
                chain.proceed();               // 调用此方法前，不会有其它任务进入该处理器
            });
        })
        .build();
```
　　串行预处理器实现了让HTTP任务排队串行处理的功能，但值得一提的是：它并没有因此而阻塞任何线程！

### 全局回调监听

```java
HTTP http = HTTP.builder()
        .responseListener((HttpTask<?> task, HttpResult result) -> {
            // 所有请求响应后都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnResponse 回调，false 表示不再执行
        })
        .completeListener((HttpTask<?> task, State state) -> {
            // 所有请求执行完都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnComplete 回调，false 表示不再执行
        })
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有请求发生异常都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnException 回调，false 表示不再执行
        })
        .build();
```

　　全局回调监听与拦截器的异同:
> * 拦截器可以添加多个，全局回调监听分三种，每种最多添加一个
> * 拦截器处的理时机在请求前和响应后，全局回调监听只在响应后，并且晚于拦截器
> * 全局回调监听可以 **阻断**（return false）某个请求的具体回调，而拦截器不能

### 全局下载监听

```java
HTTP http = HTTP.builder()
        .downloadListener((HttpTask<?> task, Download download) -> {
            // 所有下载在开始之前都会先走这里
            Ctrl ctrl = download.getCtrl();         // 下载控制器
            
        })
        .build();
```

## 使用 HttpUtils 类

　　类`HttpUtils`本是 [前身 HttpUtils](https://gitee.com/ejlchina-zhxu/httputils) 的 1.x 版本里的最重要的核心类，由于在后来的版本里抽象出了`HTTP`接口，使得它的重要性已不如往昔。但合理的使用它，仍然可以带来不少便利，特别是在没有IOC容器的环境里，比如在Android开发和一些工具项目的开发中。

　　类`HttpUtils`共定义了四个静态方法：
 
* `of(HTTP http)`      配置`HttpUtils`持有的`HTTP`实例（不调用此方法前默认使用一个没有没有经过任何配置的`HTTP`懒实例）
* `async(String url)`  开始一个异步请求 （内部通过一个`HTTP`单例实现）
* `async()`            开始一个异步HTTP任务，使用该方法必须在构建时设置 BaseUrl
* `sync(String url)`   开始一个同步请求 （内部通过一个`HTTP`单例实现）
* `sync()`             开始一个同步HTTP任务，使用该方法必须在构建时设置 BaseUrl
* `cancel(String tag)` 按标签取消请求（内部通过一个`HTTP`单例实现）
* `cancelAll()`        取消所有HTTP任务，包括同步和异步（内部通过一个`HTTP`单例实现）
* `request(Request request)`  OkHttp 原生请求 （该请求不经过 预处理器）
* `webSocket(Request request, WebSocketListener listener)` WebSocket通讯
　　也就是说，能使用`http`实例的地方，都可以使用`HttpUtils`类，例如：

```java
// 在配置HTTP实例之前，只能使用全路径方式
List<Role> roles = HttpUtils.sync("http://api.demo.com/roles")
        .get().getBody().toList(Role.class);

// 配置HTTP实例,全局生效
HttpUtils.of(HTTP.builder()
        .baseUrl("http://api.demo.com")
        .build());

// 内部使用新的HTTP实例
List<User> users = HttpUtils.sync("/users")
        .get().getBody().toList(User.class);
```


## 异常处理

　　使用 OkHttps 时，**异常处理不是必须的**，但相比其它的 HTTP 开发包，它还提供一个特别的处理方法：`nothrow()`，以满足不同的异常处理需求。

### 同步请求的异常

　　默认情况下，当同步请求执行异常时，会直接向外抛出，我们可以用 `try catch` 来捕获，例如：

```java
try {
    HttpResult result = http.sync("/users/1").get();
} catch (HttpException e) {
    Throwable cause = e.getCause(); // 得到异常原因
    if (cause instanceof ConnectException) {
        // 当没网络时，会抛出连接异常
    }
    if (cause instanceof SocketTimeoutException) {
        // 当接口长时间未响应，会抛出超时异常
    }
    if (cause instanceof UnknownHostException) {
        // 当把域名或IP写错，会抛出 UnknownHost 异常
    }
    // ...
}
``` 
　　这种传统的异常处理方式，当然可以解决问题，但 OkHttps 有更佳的方案：

```java
// 方法  nothrow() 让异常不直接抛出
HttpResult result = http.sync("/users/1").nothrow().get();
// 判断执行状态
switch (result.getState()) {
    case RESPONSED:     // 请求已正常响应
        break;
    case CANCELED:      // 请求已被取消
        break;
    case NETWORK_ERROR: // 网络错误，说明用户没网了
        break;
    case TIMEOUT:       // 请求超时
        break;
    case EXCEPTION:     // 其它异常
        break;
}
// 还可以获得具体的异常信息
IOException error = result.getError();
``` 

### 异步请求的异常

　　异步请求最常用的异常处理方式就是设置一个异常回调：

```java
http.async("/users/1")
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .setOnException((IOException e) -> {
            // 这里处理请求异常
        })
        .get();
```
　　当然，还有一个全局异常监听（`ExceptionListener`）：

```java
HTTP http = HTTP.builder()
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有请求发生异常都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnException 回调，false 表示不再执行
        })
        .build();
```
　　如果不设置`OnException`回调，也没有`ExceptionListener`，发生异常时会在 **IO 线程** 中向上抛出，外层无法捕获：

```java
try {
    http.async("/users/1")
            .setOnResponse((HttpResult result) -> {
                // 当发生异常时就不会走这里
            })
            .get();
} catch (HttpException e) {
    // 这种方式是捕获不到异常的！！！！！！
}
```
　　即使没有`OnException`回调，发生异常时，依然会走`OnComplete`回调，如果设置了的话：

```java
http.async("/users/1")
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .setOnComplete((State state) -> {
            // 发生异常，会先执行这里，可以根据 state 判断发生了什么
            // 但执行完后依然会在IO线程中向上抛出
        })
        .get();
```
　　如果就是想 **不处理异常，也不向上抛出**，发生错误完全无视，可以做到吗？可以！还是使用`nothrow()`方法：

```java
http.async("/users/1")
        .nothrow()  // 告诉 OkHttps 发生异常时不向外抛出
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .get();
```
## 取消请求的4种方式

　　在 OkHttps 里取消请求共有 **4 种** 方式可选：

* 使用`HttpCall#cancel()`取消单个请求（适用于异步请求，[详见 3.3 章节](#33-httpcall)）
* 使用`HttpTask#cancel()`取消单个请求（适用于所有请求）（since v1.0.4）

```java
HttpTask<?> task = http.async("/users")
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        });

task.get(); // 发起 GET 请求

// 取消请求，并返回是否取消成功
boolean canceled = task.cancel();   
```

* 使用`HTTP#cancel(String tag)`按标签批量取消请求（适用于所有请求，[详见第 5 章节](#5-使用标签)）
* 使用`HTTP#cancelAll()`取消所有请求（适用于所有请求）（since v1.0.2）

```java
http.cancelAll();   // 取消所有请求
```
