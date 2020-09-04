---
description: OkHttps WebSocket Heatbeat 心跳 OkHttp
---

# WebSocket

OkHttps 使用`webSocket(String url)`方法发起 WebSocket 连接，并使用`listen()`方法启动监听。 

## 连接参数

如果在连接时需要向服务器传递参数，处理方式和 HTTP 请求一样，例如需要用户名和密码才能连接 WebSocket 服务：

```java
http.webSocket("/chat") 
        .addUrlPara("username", "Jack")
        .addUrlPara("password", "xxxxxxxx")
        .setOnMessage((WebSocket ws，Message msg) -> {

        })
        .listen();                     // 启动监听
```

::: warning 注意
WebSocket 连接只能添加 **请求头**、 **Url 参数**（查询参数）和 **Path 参数**, 报文体参数（Body）和 文件参数是不允许添加的。
:::

## 心跳机制

OkHttps 自带两种心跳机制

### 全局心跳配置

在构建`HTTP`实例时，可以配置一个全局心跳时间间隔：

```java
HTTP http = HTTP.builder()
    .config((OkHttpClient.Builder builder) -> {

        // 配置 WebSocket 心跳间隔（默认没有心跳）
        builder.pingInterval(10, TimeUnit.SECONDS);
    })
    .build();
```

如上配置，当使用这个`HTTP`实例发起 WebSocket 连接时，客户端会每隔 10秒 向服务器发送一次 PING 消息，同时服务器必须在客户端发送心跳后的 10秒 内回复 PONG 消息，否则就会触发`SocketTimeoutException`异常

### 单次心跳配置（since V2.3.0）

自 V2.3.0 起 OkHttps 提供了另外一种心跳机制，它在发起具体的 WebSocket 连接时通过方法`heatbeat(int pingSeconds, int pongSeconds)`分别指定客户端与服务器的心跳时间间隔：

```java
http.webSocket("/chat") 
        .heatbeat(10, 10)
        .setOnMessage((WebSocket ws，Message msg) -> {
            // ...
        })
        .listen();                     // 启动监听
```

如上配置，客户端仍会每隔 10秒 向服务器发送一次 PING 消息，并期望服务器回复 PONG 消息的间隔也是 10 秒一次，但如果服务器或网络由于某些未知原因导致客户端未能正确收到 PONG 消息，客户端还会容忍两次失败，当第三个 10 秒后还未收到服务器的任何消息时，则会触发`SocketTimeoutException`异常

::: tip OkHttps 的心跳机制相对于 OkHttp 主要有以下特点
* 客户端发送的任何消息都具有一次心跳作用
* 服务器发送的任何消息都具有一次心跳作用
* 若服务器超过 3 * pongSeconds 秒没有回复心跳，才触发心跳超时
:::

另外，还可以使用`pingSupplier(Supplier<ByteString> pingSupplier)`方法来指定心跳时发送的具体内容：

```java
http.webSocket("/chat") 
        .heatbeat(10, 10)
        .pingSupplier(() -> {
            // 每次心跳发送一个换行符
            return ByteString.encodeUtf8("\n");
        })
        .listen();
```

## 消息收发

### 接收消息

发起 WebSocket 连接是，设置一个`OnMessage`回调，便可接收到服务器的消息：

```java
http.webSocket("/chat") 
        .setOnMessage((WebSocket ws，Message msg) -> {
            // 接收到消息 msg
        })
        .listen();                     // 启动监听
```

在该回调内接收到一个`Message`类型的消息对象，它和`HttpReault.Body`都继承自`Toable`接口，他共有如下一些方法：

* `isText()` 判断是文本消息还是二进制消息
* `toByteStream()` 消息体转字节流
* `toBytes()` 消息体转字数组
* `toByteString()` 消息体转字数组
* `toCharStream()` 消息体转字符流
* `toString()` 消息体转字符串
* `toMapper()` 消息体转 Mapper 对象（不想定义 Java Bean 时使用）
* `toArray()` 消息体转 Array 数组（不想定义 Java Bean 时使用）
* `toBean(Class<T> type)` 报文体Json文本转JavaBean
* `toBean(Type type)` 报文体Json文本转JavaBean
* `toBean(TypeRef<T> type)` 报文体Json文本转JavaBean
* `toList(Class<T> type)` 报文体Json文本转JavaBean列表

### 发送消息

向服务器发送消息，首先要获得一个`WebSocket`实例，该实例可以通过`listen()`方法的返回值或回调方法的参数获取，如:

```java
WebSocket ws = http.webSocket("/chat").listen();  // 启动监听，并返回一个 WebSocket 实例
```

接口`WebSocket`继承自`Cancelable`，它共定义了如下方法：

* `cancel()` 取消连接（连接成功前可以取消）
* `queueSize()` 排队待发送消息的数量
* `send(Object object)` 发送消息，参数是待发送的对象，可以是 String | ByteString | byte[] | Java Bean
* `close(int code, String reason)` 关闭连接（连接成功后可以关闭）
* `msgType(String type)` 设置消息传输类型，类似于`bodyType`

一个发送消息的例子

```java
WebSocket ws = http.webSocket("/chat").listen();
ws.send("Hello World!")     // 该消息会先进入排队等待状态，当连接成功时发送给服务器
```

或者

```java
http.webSocket("/chat") 
        .setOnOpen((WebSocket ws, HttpResult res) -> {
            // 当连接成功时发送给服务器
            ws.send("Hello World!")
        })
        .listen();
```

## 回调方法

WebSocket 连接共可设置 **5** 种回调方法： 

```java
http.webSocket("/websocket-endpoint")
        .setOnOpen((WebSocket ws, HttpResult res) -> {
            // WebSocket 连接成功时回调
        })
        .setOnMessage((WebSocket ws, Message msg) -> {
            // 收到服务器下发的消息时回调
        })
        .setOnException((WebSocket ws, Throwable thr) -> {
            // 连接发生异常时回调
        })
        .setOnClosing((WebSocket ws, WebSocket.Close close) -> {
            // 连接正在关闭时回调
        })
        .setOnClosed((WebSocket ws, WebSocket.Close close) -> {
            // 连接已关闭（v2.0.0 之后包含连接被取消 和 连接发生异常）时回调
        })
        .listen();
```

::: tip 需要注意的是
如果设置了 [全局回调监听](/v2/configuration.html#全局回调监听), 它们对 WebSocket 连接 同样起作用
:::


<br/>

<Vssue :title="$title" />