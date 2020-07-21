---
description: OkHttps 配置 BaseUrl 回调执行器 主线程 UI线程 OkHttpClient 预处理器 TOKEN问题最佳解决方案 刷新TOKEN 全局 回调监听 下载监听 回调阻断 拦截器 CookieJar、SSL、缓存、代理、事件监听
---

# WebSocket

OkHttps 使用`webSocket()`方式发起 WebSocket 连接，并使用`listen()`方法启动监听。 

## WebSocket 请求

如果在连接时需要向服务器传递参数，处理方式和 HTTP 请求一样，例如需要用户名和密码才能连接 WebSocket 服务：

```java
http.webSocket("/chat") 
        .addUrlPara("username", "Jack")
        .addUrlPara("password", "xxxxxxxx")
        .setOnMessage((WebSocket ws，Message msg) -> {

        })
        .listen();                     // 启动监听
```

### 心跳机制

OkHttps 自带两种心跳机制

#### OkHttp 的心跳机制

在构建`HTTP`实例时，配置一个全局心跳间隔：

```java
HTTP http = HTTP.builder()
    .config((OkHttpClient.Builder builder) -> {

        // 配置 WebSocket 心跳间隔（默认没有心跳）
        builder.pingInterval(10, TimeUnit.SECONDS);
    })
    .build();
```

如上配置，当使用这个`HTTP`实例发起 WebSocket 连接时，客户端会每隔 10秒 向服务器发送一次 PING 消息，同时服务器必须在客户端发送心跳后的 10秒 内回复 PONG 消息，否则就会触发`SocketTimeoutException`异常

#### OkHttps 的心跳机制

OkHttps 提供了另外一种心跳机制，它在发起具体的 WebSocket 连接时通过`heatbeat(int pingSeconds, int pongSeconds)`方法指定：

```java
http.webSocket("/chat") 
        .heatbeat(10, 10)
        .setOnMessage((WebSocket ws，Message msg) -> {

        })
        .listen();                     // 启动监听
```

如上配置，客户端仍会每隔 10秒 向服务器发送一次 PING 消息，并期望服务器回复 PONG 消息的间隔也是 10 秒一次，但如果服务器或网络由于某些未知原因导致客户端未能正确收到 PONG 消息，客户端还会容忍两次失败，当第三个 10 秒后还未收到服务器的任何消息时，则会触发`SocketTimeoutException`异常

::: tip OkHttps 的心跳机制相对于 OkHttp 主要有以下特点
* 客户端发送的任何消息都具有一次心跳作用
* 服务器发送的任何消息都具有一次心跳作用
* 若服务器超过 3 * pongSeconds 秒没有回复心跳，才触发心跳超时
:::


<br/>

<Vssue :title="$title" />