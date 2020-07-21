---
description: OkHttps WebSocket Heatbeat 心跳 OkHttp
---

# Stomp

项目 [OkHttps Stomp](https://gitee.com/ejlchina-zhxu/okhttps-stomp) 基于 OkHttps 的 [WebSocket](/v2/websocket.html) 功能实现了一个非常轻量的 Stomp 客户端协议，它可以让你轻松实现 **广播发布与订阅** 和 **队列发布与订阅** 的客户端功能。

## 构建 Stomp 实例

类`Stomp`提供静态方法`over`来构建`Stomp`实例：

* `over(WebSocketTask task)` 基于 OkHttps 的 WebSocket 构建一个 Stomp 实例，并自动确认消息
* `over(WebSocketTask task, boolean autoAck)` 构建一个 Stomp 实例，并可指定是否自动确认消息

例如：

```java
// 使用一个 Websocket 连接构建一个 Stomp 实例，同时设置心跳间隔为 20 秒
Stomp stomp = Stomp.over(OkHttps.webSocket("wss://...").heatbeat(20, 20));
```

以上代码构建了一个简单的 Stomp 客户端，并默认在收到消息时会**自动确认**，如果需要收到确认，可以使用下面的方式：

```java
Stomp stomp = Stomp.over(
        OkHttps.webSocket("wss://...").heatbeat(20, 20),
        false       // 参数设置 autoAck 为 false，将需要手动确认消息
    );
```

## 连接 Stomp 服务

得到一个`Stomp`实例后，可使用以下两个方法连接 Stomp 服务器：

* `connect()` 直接连接 Stomp 服务器
* `connect(List<Header> headers)` 携带一些 Stomp 头信息连接 Stomp 服务器

例如：

```java
stomp.connect();
```

如果服务器在连接时需要指令一些额外信息，比如连接 RabbitMQ 时需要指定`login`、`passcode` 和 `vhost`: 

```java
List<Header> headers = new ArrayList<>();
headers.add(new Header("login", "username"));
headers.add(new Header("passcode", "xxxxxxx"));
headers.add(new Header("host", "your_vhost"));

stomp.connect(headers);
```

## 连接状态监听

```java
Stomp.over(OkHttps.webSocket("wss://...").heatbeat(20, 20))
    .setOnConnected(stomp -> {
        // 服务器连接成功回调
    })
    .setOnDisconnected(close -> {
        // 连接已断开回调
    })
    .connect();
```

## 消息订阅与退订

### 订阅广播

```java
stomp.topic("/your-topic", (Message msg) -> {
    // 得到消息负载
    String payload = msg.getPayload();

    // 如果需要手动确认消息,调用此方法确认
    stomp.ack(msg)
});
```

### 退订广播

```java
stomp.untopic("/your-topic");
```

### 订阅队列

```java
stomp.queue("/your-queue", (Message msg) -> {
    // 得到消息负载
    String payload = msg.getPayload();

    // 如果需要手动确认消息,调用此方法确认
    stomp.ack(msg)
});
```

### 退订队列

```java
stomp.unqueue("/your-queue");
```

## 发送消息

```java
// 发送到广播
stomp.sendTo("/topic/your-topic", "Hello World");
// 发送到队列
stomp.sendTo("/queue/your-queue", "Hello World");
```

## 断开服务

```java
stomp.disconnect();
```

<br/>

<Vssue :title="$title" />