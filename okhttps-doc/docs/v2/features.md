---
description: OkHttps 使用 HttpUtils 类 WebSocket 异常处理 nothrow 异常回调 多种方式 取消请求 标签 批量取消 cancelAll
---

# 特色

#### 哎呀，作者还在加班中，2.x 的文档很快就出来，先看一下 1.x 的文档吧 :joy:

## 使用 HttpUtils 类

　　类`HttpUtils`本是 [前身 HttpUtils](https://gitee.com/ejlchina-zhxu/httputils) 的 1.x 版本里的最重要的核心类，由于在后来的版本里抽象出了`HTTP`接口，使得它的重要性已不如往昔。但合理的使用它，仍然可以带来不少便利，特别是在没有IOC容器的环境里，比如在Android开发和一些工具项目的开发中。

　　类`HttpUtils`共定义了四个静态方法：
 
* `of(HTTP http)`      配置`HttpUtils`持有的`HTTP`实例（不调用此方法前默认使用一个没有没有经过任何配置的`HTTP`懒实例）
* `sync(String url)`   开始一个同步请求 （内部通过一个`HTTP`单例实现）
* `async(String url)`  开始一个异步请求 （内部通过一个`HTTP`单例实现）
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

## 取消请求

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