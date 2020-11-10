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
        .addBodyParam("username", "Jackson")
        .addBodyParam("password", "xxxxxx")
        .post();                           // POST请求
```
　　在配置了`BaseUrl`之后，如有特殊请求任务，仍然可以使用全路径的方式，一点都不妨碍：

```java
http.sync("https://www.baidu.com").get();
```

## 回调执行器

　　OkHttps 默认所有回调都在 **IO线程** 执行，如何想改变执行回调的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在UI线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置默认 **影响所有回调**。

## 配置 OkHttpClient

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

## 并行预处理器

　　预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与`OkHttp`的拦截器（`Interceptor`）不同：预处理器可以让我们 **异步** 处理这些问题。

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

## 串行预处理器（TOKEN问题最佳解决方案）

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
                chain.proceed();               // 调用此方法前，不会有其它任务进入该处理器
            });
        })
        .build();
```
　　串行预处理器实现了让HTTP任务排队串行处理的功能，但值得一提的是：它并没有因此而阻塞任何线程！

## 全局回调监听

　　全局回调是实际开发中经常需要的功能，比如对服务器响应的状态码进行统一处理等，同时 OkHttps 的全局回调还具有 **回调阻断** 的功能：

```java
HTTP http = HTTP.builder()
        .responseListener((HttpTask<?> task, HttpResult result) -> {
            // 所有异步请求（包括 WebSocket）响应后都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnResponse 回调，false 表示不再执行
        })
        .completeListener((HttpTask<?> task, State state) -> {
            // 所有异步请求（包括 WebSocket）执行完都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnComplete 回调，false 表示不再执行
        })
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有异步请求（包括 WebSocket）发生异常都会走这里

            return true; // 返回 true 表示继续执行 task 的 OnException 回调，false 表示不再执行
        })
        .build();
```

::: tip 全局回调监听与拦截器的异同：
* 拦截器可以添加多个，全局回调监听分三种，每种最多添加一个
* 拦截器处的理时机在请求前和响应后，全局回调监听只在响应后，并且晚于拦截器
* 全局回调监听可以 **阻断**（return false）某个请求的具体回调，而拦截器不能
:::

## 全局下载监听

```java
HTTP http = HTTP.builder()
        .downloadListener((HttpTask<?> task, Download download) -> {
            // 所有下载在开始之前都会先走这里
            Ctrl ctrl = download.getCtrl();         // 下载控制器
            
        })
        .build();
```