---
home: true
heroImage: /logo.png
heroText: OkHttps V2
tagline: 强大轻量 且 前后端通用的 HTTP 客户端
actionText: 极速上手 →
actionLink: /v2/
features:
- title: 轻量纯粹优雅
  details: OkHttps 非常轻量，体积仅是 Retrofit 的一半不到，并且不依赖于特定平台，API 语义简洁舒适。
- title: 开箱即用的功能
  details: 异步预处理器、回调执行器、全局监听器、回调阻断机制、文件上传下载、过程控制、进度监听。
- title: 更多实用特性
  details: URL 占位符、Lambda 回调、JSON自动封装解析、OkHttp 的特性：拦截器、连接池、CookieJar 等。

footer: Apache Licensed | Copyright © 2020-present ejlchina
---

#### <center> 如艺术一般优雅，像 1、2、3 一样简单 </center>

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.example.com")
        .addMsgConvertor(new GsonMsgConvertor());
        .build();

// 同步 HTTP
List<User> users = http.sync("/users") 
        .get()                          // GET请求
        .getBody()                      // 响应报文体
        .toList(User.class);            // 自动反序列化 List 

// 异步 HTTP
http.async("/users/1")
        .setOnResponse((HttpResult res) -> {
            // // 自动反序列化 Bean 
            User user = res.getBody().toBean(User.class);
        })
        .get();                         // GET请求

// WebSocket
http.webSocket("/chat") 
        .onMessage((WebSocket ws, Message msg) -> {
            // 从服务器接收消息
            Chat chat = msg.toBean(Chat.class);
            // 向服务器发送消息
            ws.send(chat); 
        })
        .listen();                     // 启动监听
```

[<center> 了解更多 </center>](/v2/)
