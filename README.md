# OkHttps

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/okhttps/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/okhttps/)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/LICENSE)
[![Troy.Zhou](https://img.shields.io/badge/%E4%BD%9C%E8%80%85-ejlchina-orange.svg)](https://github.com/ejlchina)

## 介绍

　　OkHttps 是近期开源的对 OkHttp3 轻量封装的框架，它独创的异步预处理器，特色的标签，灵活的上传下载进度监听与过程控制功能，在轻松解决很多原本另人头疼问题的同时，设计上也力求纯粹与优雅。

 * 链式调用，一点到底
 * BaseURL、URL占位符、JSON自动封装与解析
 * 同步拦截器、异步预处理器、回调执行器、全局监听、回调阻断
 * 文件上传下载（过程控制、进度监听）
 * 单方法回调，充分利用 Lambda 表达式
 * TCP连接池、Http2

> OkHttps 非常轻量，自身代码仅 2000 行左右，且有较高的编码质量，sonarqube 分析结果为 3A，[查看报告](https://gitee.com/ejlchina-zhxu/okhttps/quality_analyses?platform=sonar_qube)

### 前世：[HttpUtils, V2.3.0 之后重命名为 OkHttps](https://gitee.com/ejlchina-zhxu/httputils)
### 当前文档版本[1.0.3]
### API文档：[https://apidoc.gitee.com/ejlchina-zhxu/okhttps](https://apidoc.gitee.com/ejlchina-zhxu/okhttps)

## 目录

+ [安装教程](#安装教程)
  + [Maven](#maven)
  + [Gradle](#gradle)
+ [使用说明](#使用说明)
  + [1 简单示例](#1-简单示例)
    - [1.1 构建HTTP](#11-构建-http)
    - [1.2 同步请求](#12-同步请求)
    - [1.3 异步请求](#13-异步请求)
  + [2 请求方法（GET|POST|PUT|DELETE）](#2-请求方法getpostputdelete)
  + [3 解析请求结果](#3-解析请求结果)
    - [3.1 回调函数](#31-回调函数)
    - [3.2 HttpResult](#32-HttpResult)
    - [3.3 HttpCall](#33-HttpCall)
  + [4 构建HTTP任务](#4-构建HTTP任务)
  + [5 使用标签](#5-使用标签)
  + [6 配置 HTTP](#65-配置-http)
    - [6.1 设置 BaseUrl](#61-设置-baseurl)
    - [6.2 回调执行器](#62-回调执行器)
    - [6.3 配置 OkHttpClient](#63-配置-okhttpclient)
    - [6.4 并行预处理器](#64-并行预处理器)
    - [6.5 串行预处理器（TOKEN问题最佳解决方案）](#65-串行预处理器token问题最佳解决方案)
    - [6.6 全局回调监听](#66-全局回调监听)
    - [6.7 全局下载监听](#67-全局下载监听)
  + [7 使用 HttpUtils 类](#7-使用-httputils-类)
  + [8 文件下载](#8-文件下载)
    - [8.1 下载进度监听](#81-下载进度监听)
    - [8.2 下载过程控制](#82-下载过程控制)
    - [8.3 实现断点续传](#83-实现断点续传)
    - [8.4 实现分块下载](#84-实现分块下载)
  + [9 文件上传](#9-文件上传)
    - [9.1 上传进度监听](#91-上传进度监听)
    - [9.2 上传过程控制](#92-上传过程控制)
  + [10 异常处理](#10-异常处理)
    - [10.1 同步请求的异常](#101-同步请求的异常)
    - [10.2 异步请求的异常](#102-异步请求的异常)
  + [11 取消请求的4种方式](#11-取消请求的4种方式)
  + [12 回调线程自由切换（for Android）](#12-回调线程自由切换for-android)
  + [13 实现生命周期绑定（for Android）](#13-实现生命周期绑定for-android)
+ [后期计划（v1.1.0）](#后期计划v110)
+ [联系方式](#联系方式)
+ [参与贡献](#参与贡献)

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps</artifactId>
     <version>1.0.4</version>
</dependency>
```
### Gradle

`implementation 'com.ejlchina:okhttps:1.0.4'`

## 使用说明

### 1 简单示例

#### 1.1 构建 HTTP

```java
HTTP http = HTTP.builder().build();
```
　　以上代码构建了一个最简单的`HTTP`实例，它拥有以下方法：

* `async(String url)`  开始一个异步请求 （内部通过一个`HTTP`单例实现）
* `async()`            开始一个异步HTTP任务，使用该方法必须在构建时设置 BaseUrl
* `sync(String url)`   开始一个同步请求 （内部通过一个`HTTP`单例实现）
* `sync()`             开始一个同步HTTP任务，使用该方法必须在构建时设置 BaseUrl
* `cancel(String tag)` 按标签取消请求（内部通过一个`HTTP`单例实现）
* `cancelAll()`        取消所有HTTP任务，包括同步和异步（内部通过一个`HTTP`单例实现）
* `request(Request request)`  OkHttp 原生请求 （该请求不经过 预处理器）
* `webSocket(Request request, WebSocketListener listener)` WebSocket通讯

　　为了使用方便，在构建的时候，我们更愿意指定一个`BaseUrl`（请参见[5.1 设置 BaseUrl](#51-设置-baseurl)）:

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")
        .build();
```
　　为了简化文档，下文中出现的`http`均是在构建时设置了`BaseUrl`的`HTTP`实例。

#### 1.2 同步请求

　　使用方法`sync(String url)`开始一个同步请求：

```java
List<User> users = http.sync("/users") // http://api.demo.com/users
        .get()                         // GET请求
        .getBody()                     // 获取响应报文体
        .toList(User.class);           // 得到目标数据
```
　　方法`sync`返回一个同步`HttpTask`，可链式使用。

#### 1.3 异步请求

　　使用方法`async(String url)`开始一个异步请求：

```java
http.async("/users/1")                //  http://api.demo.com/users/1
        .setOnResponse((HttpResult result) -> {
            // 得到目标数据
            User user = result.getBody().toBean(User.class);
        })
        .get();                       // GET请求
```
　　方法`async`返回一个异步`HttpTask`，可链式使用。

### 2 请求方法（GET|POST|PUT|DELETE）

　　同步与异步的`HttpTask`都拥有`get`、`post`、`put`与`delete`方法。不同的是：同步`HttpTask`的这些方法返回一个`HttpResult`，而异步`HttpTask`的这些方法返回一个`HttpCall`。

```java
HttpResult res1 = http.sync("/users").get();     // 同步 GET
HttpResult res2 = http.sync("/users").post();    // 同步 POST
HttpResult res3 = http.sync("/users/1").put();   // 同步 PUT
HttpResult res4 = http.sync("/users/1").delete();// 同步 DELETE
HttpCall call1 = http.async("/users").get();     // 异步 GET
HttpCall call2 = http.async("/users").post();    // 异步 POST
HttpCall call3 = http.async("/users/1").put();   // 异步 PUT
HttpCall call4 = http.async("/users/1").delete();// 异步 DELETE
```
### 3 解析请求结果

#### 3.1 回调函数

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

#### 3.2 HttpResult

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


#### 3.3 HttpCall

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

### 4 构建HTTP任务

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
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，用于分块下载
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，用于分块下载

* `bind(Object object)` 绑定一个对象，可用于实现Android里的生命周期绑定

### 5 使用标签

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
        .setTag("C")    // since v1.0.4, 标签将以追加模式添加
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

### 6 配置 HTTP

#### 6.1 设置 BaseUrl

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

#### 6.2 回调执行器

　　OkHttps 默认所有回调都在 **IO线程** 执行，如何想改变执行回调的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在UI线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置默认 **影响所有回调**。

#### 6.3 配置 OkHttpClient

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

#### 6.4 并行预处理器

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
　　和`Interceptor`一样，`Preprocessor`也可以添加多个。他们之前的区别如下:

> * 拦截器只能处理同步操作，预处理器支持处理异步操作
> * 拦截器都是并行处理请求，预处理器支持串行处理（详见6.5章节）
> * 拦截器处理时机在请求前和响应后，预处理器只在请求前，并且先于拦截器执行。关于响应后，OkHttps还提供了全局回调监听（详见6.6章节）

#### 6.5 串行预处理器（TOKEN问题最佳解决方案）

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

#### 6.6 全局回调监听

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

#### 6.7 全局下载监听

```java
HTTP http = HTTP.builder()
        .downloadListener((HttpTask<?> task, Download download) -> {
            // 所有下载在开始之前都会先走这里
            Ctrl ctrl = download.getCtrl();         // 下载控制器
            
        })
        .build();
```

### 7 使用 HttpUtils 类

　　类`HttpUtils`本是 1.x 版本里的最重要的核心类，由于在 2.x 版本里抽象出了`HTTP`接口，使得它的重要性已不如往昔。但合理的使用它，仍然可以带来不少便利，特别是在没有IOC容器的环境里，比如在Android开发和一些工具项目的开发中。

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

### 8 文件下载

　　OkHttps 并没有把文件的下载排除在常规的请求之外，同一套API，它优雅的设计使得下载与常规请求融合的毫无违和感，一个最简单的示例：

```java
http.sync("/download/test.zip")
        .get()                           // 使用 GET 方法（其它方法也可以，看服务器支持）
        .getBody()                       // 得到报文体
        .toFile("D:/download/test.zip")  // 下载到指定的路径
        .start();                        // 启动下载

http.sync("/download/test.zip").get().getBody()                  
        .toFolder("D:/download")         // 下载到指定的目录，文件名将根据下载信息自动生成
        .start();
```
　　或使用异步连接方式：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            result.getBody().toFolder("D:/download").start();
        })
        .get();
```
　　这里要说明一下：`sync`与`async`的区别在于连接服务器并得到响应这个过程的同步与异步（这个过程的耗时在大文件下载中占比极小），而`start`方法启动的下载过程则都是异步的。

#### 8.1 下载进度监听

　　就直接上代码啦，诸君一看便懂：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setStepBytes(1024)   // 设置每接收 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每接收 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 下载进度回调
            long doneBytes = process.getDoneBytes();   // 已下载字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已下载的比例
            boolean isDone = process.isDone();         // 是否下载完成
        })
        .toFolder("D:/download/")        // 指定下载的目录，文件名将根据下载信息自动生成
 //     .toFile("D:/download/test.zip")  // 指定下载的路径，若文件已存在则覆盖
        .setOnSuccess((File file) -> {   // 下载成功回调
            
        })
        .start();
```
　　值得一提的是：由于 OkHttps 并没有把下载做的很特别，这里设置的进度回调不只对下载文件起用作，即使对响应JSON的常规请求，只要设置了进度回调，它也会告诉你报文接收的进度（提前是服务器响应的报文有`Content-Length`头），例如：

```java
List<User> users = http.sync("/users")
        .get()
        .getBody()
        .setStepBytes(2)
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toList(User.class);
```

#### 8.2 下载过程控制

　　过于简单：还是直接上代码：

```java
Ctrl ctrl = http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toFolder("D:/download/")
        .start();   // 该方法返回一个下载过程控制器
 
ctrl.status();      // 下载状态
ctrl.pause();       // 暂停下载
ctrl.resume();      // 恢复下载
ctrl.cancel();      // 取消下载（同时会删除文件，不可恢复）
```
　　无论是同步还是异步发起的下载请求，都可以做以上的控制：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            // 拿到下载控制器
            Ctrl ctrl = result.getBody().toFolder("D:/download/").start();
        })
        .get();
```

#### 8.3 实现断点续传

　　OkHttps 对断点续传并没有再做更高层次的封装，因为这是app该去做的事情，它在设计上使各种网络问题的处理变简单的同时力求纯粹。下面的例子可以看到，OkHttps 通过一个失败回调拿到 **断点**，便将复杂的问题变得简单：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .toFolder("D:/download/")
        .setOnFailure((Failure failure) -> {         // 下载失败回调，以便接收诸如网络错误等失败信息
            IOException e = failure.getException();  // 具体的异常信息
            long doneBytes = failure.getDoneBytes(); // 已下载的字节数（断点），需要保存，用于断点续传
            File file = failure.getFile();           // 下载生成的文件，需要保存 ，用于断点续传（只保存路径也可以）
        })
        .start();
```
　　下面代码实现续传：

```java
long doneBytes = ...    // 拿到保存的断点
File file =  ...        // 待续传的文件

http.sync("/download/test.zip")
        .setRange(doneBytes)                         // 设置断点（已下载的字节数）
        .get()
        .getBody()
        .toFile(file)                                // 下载到同一个文件里
        .setAppended()                               // 开启文件追加模式
        .setOnSuccess((File file) -> {

        })
        .setOnFailure((Failure failure) -> {
        
        })
        .start();
```

#### 8.4 实现分块下载

　　当文件很大时，有时候我们会考虑分块下载，与断点续传的思路是一样的，示例代码：

```java
static String url = "http://api.demo.com/download/test.zip"

public static void main(String[] args) {
    long totalSize = HttpUtils.sync(url).get().getBody()
            .close()             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
            .getContentLength(); // 获得待下载文件的大小（由于未消费报文体，所以该请求不会消耗下载报文体的时间和网络流量）
    download(totalSize, 0);      // 从第 0 块开始下载
    sleep(50000);                // 等待下载完成（不然本例的主线程就结束啦）
}

static void download(long totalSize, int index) {
    long size = 3 * 1024 * 1024;                 // 每块下载 3M  
    long start = index * size;
    long end = Math.min(start + size, totalSize);
    HttpUtils.sync(url)
            .setRange(start, end)                // 设置本次下载的范围
            .get().getBody()
            .toFile("D:/download/test.zip")      // 下载到同一个文件里
            .setAppended()                       // 开启文件追加模式
            .setOnSuccess((File file) -> {
                if (end < totalSize) {           // 若未下载完，则继续下载下一块
                    download(totalSize, index + 1); 
                } else {
                    System.out.println("下载完成");
                }
            })
            .start();
}
```

### 9 文件上传

　　一个简单文件上传的示例：

```java
http.sync("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()     // 上传发法一般使用 POST 或 PUT，看服务器支持
```
　　异步上传也是完全一样：

```java
http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()
```

#### 9.1 上传进度监听

　　OkHttps 的上传进度监听，监听的是所有请求报文体的发送进度，示例代码：

```java
http.sync("/upload")
        .addBodyParam("name", "Jack")
        .addBodyParam("age", 20)
        .addFileParam("avatar", "D:/image/avatar.jpg")
        .setStepBytes(1024)   // 设置每发送 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 上传进度回调
            long doneBytes = process.getDoneBytes();   // 已发送字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已发送的比例
            boolean isDone = process.isDone();         // 是否发送完成
        })
        .post()
```
　　咦！怎么感觉和下载的进度回调的一样？没错！OkHttps 还是使用同一套API处理上传和下载的进度回调，区别只在于上传是在`get/post`方法之前使用这些API，下载是在`getBody`方法之后使用。很好理解：`get/post`之前是准备发送请求时段，有上传的含义，而`getBody`之后，已是报文响应的时段，当然是下载。

#### 9.2 上传过程控制

　　上传文件的过程控制就很简单，和常规请求一样，只有异步发起的上传可以取消：

```java
HttpCall call = http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .post()

call.cancel();  // 取消上传
```
　　上传就没有暂停和继续这个功能啦，应该没人有这个需求吧?

### 10 异常处理

　　使用 OkHttps 时，**异常处理不是必须的**，但相比其它的 HTTP 开发包，它还提供一个特别的处理方法：`nothrow()`，以满足不同的异常处理需求。

#### 10.1 同步请求的异常

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

#### 10.2 异步请求的异常

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
### 11 取消请求的4种方式

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

### 12 回调线程自由切换（for Android）

　　在 Android 开发中，经常会把某些代码放到特点的线程去执行，比如网络请求响应后的页面更新在主线程（UI线程）执行，而保存文件则在IO线程操作。OkHttps 为这类问题提供了良好的方案。

　　在 **默认** 情况下，**所有回调** 函数都会 **在 IO 线程** 执行。为什么会设计如此呢？这是因为 OkHttps 只是纯粹的 Java 领域 Http工具包，本身对 Android 不会有任何依赖，因此也不知 Android 的 UI 线程为何物。这么设计也让它在 Android 之外有更多的可能性。

　　但是在 Android 里使用  OkHttps 的话，UI线程的问题能否优雅的解决呢？当然可以！简单粗暴的方法就是配置一个 回调执行器：

 ```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            // 实际编码中可以吧 Handler 提出来，不需要每次执行回调都重新创建
            new Handler(Looper.getMainLooper()).post(run); // 在主线程执行
        })
        .build();
```
　　上述代码便实现了让 **所有** 的 **回调函数** 都在 **主线程（UI线程）** 执行的目的，如：

```java
http.async("/users")
        .addBodyParam("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .setOnResponse((HttpResult result) -> {
            // 在主线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行
        })
        .setOnComplete((State state) -> {
            // 在主线程执行
        })
        .post();
```
　　但是，如果同时还想让某些回调放在IO线程，实现 **自由切换**，怎么办呢？OkHttps 给出了非常灵活的方法，如下：

```java
http.async("/users")
        .addBodyParam("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnResponse((HttpResult result) -> {
            // 在 IO 线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行（没有指明 nextOnIO 则在回调执行器里执行）
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnComplete((State state) -> {
            // 在 IO 线程执行
        })
        .post();
```
　　无论是哪一个回调，都可以使用`nextOnIO()`方法自由切换。同样，对于文件下载也是一样：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .toFolder("D:/download/")
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnSuccess((File file) -> {
            // 在 IO 线程执行
        })
        .setOnFailure((Failure failure) -> {
            // 在主线程执行
        })
        .start();
```
### 13 实现生命周期绑定（for Android）

　　由于 OkHttps 并不依赖于 Android，所以它并没有提供关于生命周期绑定的直接实现，但它的一些扩展机制让我们很容易就可以实现这个需求。在开始之前，我们首先要理解何为生命周期绑定：

> 所谓的生命周期绑定：即是让 HTTP 任务感知其所属的 Activity 或 Fragment 的生命周期，当  Activity 或 Fragment 将被销毁时，框架应自动的把由它们发起的但尚未完成的 HTTP 任务全部取消，以免导致程序出错！

　　现在我们需要对`HTTP`实例进行配置，配置后的`HTTP`实例具有生命周期绑定的功能，在`androidx`的开发环境里，它的使用效果如下：

```java
// 在 Activity 或 Fragment 内发起请求
http.async("http://www.baidu.com")
        .bind(getLifecycle())   // 绑定生命周期
        .setOnResponse((HttpResult result) -> {
            Log.i("FirstFragment", "收到请求：" + result.toString());
        })
        .get();
```
　　上述代码中的`getLifecycle()`是`androidx`中`Activity`或`Fragment`自带的方法，而`bind()`是`HttpTask`的现有方法。在配置好`HTTP`实例后，上述代码发起的请求便可以感知`Activity`或`Fragment`的生命周期。

　　那`HTTP`实例到底该如何配置呢？

#### 第一步：配置预处理器

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .addPreprocessor((Preprocessor.PreChain chain) -> {
            HttpTask<?> task = chain.getTask();
            Object bound = task.getBound();
            // 判断 task 是否绑定了 Lifecycle 对象
            if (bound instanceof Lifecycle) {
                // 重新绑定一个 生命周期监视器（LCObserver）对象，它的定义见下一步
                task.bind(new LCObserver(task, (Lifecycle) bound));
            }
            chain.proceed();
        })
        ... // 省略其它配置项
        .build();
```

#### 第二步：定义生命周期监视器

```java
public class LCObserver implements LifecycleObserver {

    HttpTask<?> task;
    Lifecycle lifecycle;

    LCObserver(HttpTask<?> task, Lifecycle lifecycle) {
        this.task = task;
        this.lifecycle = lifecycle;
        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        task.cancel();  // 在 ON_STOP 事件中，取消对应的 HTTP 任务
    }

    public void unbind() {
        // 在请求完成之后移除监视器
        lifecycle.removeObserver(this);
    }

}
```

#### 第三步：配置全局回调监听

　　以上两步其实已经实现了生命周期绑定的功能，但是在请求完成之后，我们需要在`lifecycle`中移除`LCObserver`对象：

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .completeListener((HttpTask<?> task, HttpResult.State state) -> {
            Object bound = task.getBound();
            // 判断 task 是否绑定了生命周期监视器（LCObserver）对象
            if (bound instanceof LCObserver) {
                // 解绑监视器
                ((LCObserver) bound).unbind();
            }
            return true;
        })
        ... // 省略其它配置项
        .build();
```

**以上三步便在Android中实现了生命周期与HTTP请求绑定的功能**

　　非常简单，懒得敲代码的同学还可以 [点这里 OkHttps.java](https://gitee.com/ejlchina-zhxu/okhttps-android-demo/blob/master/app/src/main/java/com/flower/myapplication/http/OkHttps.java) 直接下载封装好的源码，其中不仅编写了生命周期绑定的配置，还有在UI线程执行回调的配置。

　　有需要的同学，可以直接下载下来使用，还可以基于它再次扩展，比如实现自动添加 TOKEN 的功能，具体可以参考[6.5 串行预处理器（TOKEN问题最佳解决方案）](#65-串行预处理器token问题最佳解决方案)。


## 后期计划（v1.1.0）

* 简化 WebSocket 编程：可直接发送 JavaBean 对象消息
* 简化 WebSocket 编程：可直接接收 JavaBean 对象消息
* 简化 WebSocket 编程：可使用 Lambda 表达式进行 WebSocket 编程

## 联系方式

* 微信：<img src="https://images.gitee.com/uploads/images/2020/0414/003013_e7b811e4_1393412.jpeg" width="300px">
* 邮箱：zhou.xu@ejlchina.com

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
