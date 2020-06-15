<p align="center">
  <a href="http://okhttps.ejlchina.com/" target="_blank">
    <img width="128" src="https://images.gitee.com/uploads/images/2020/0511/091408_c26f1306_1393412.png" alt="logo">
  </a>
</p>
<p align="center">
    <a href="https://maven-badges.herokuapp.com/maven-central/com.ejlchina/okhttps/"><img src="https://maven-badges.herokuapp.com/maven-central/com.ejlchina/okhttps/badge.svg" alt="Maven Central"></a>
    <a href="https://gitee.com/ejlchina-zhxu/okhttps/blob/master/LICENSE"><img src="https://img.shields.io/hexpm/l/plug.svg" alt="License"></a>
    <a href="https://github.com/ejlchina"><img src="https://img.shields.io/badge/%E4%BD%9C%E8%80%85-ejlchina-orange.svg" alt="Troy.Zhou"></a>
</p>

## 文档

官网 http://okhttps.ejlchina.com/

## 为什么用

　　OkHttps 是近期开源的对 OkHttp3 轻量封装的框架，它独创的异步预处理器，特色的标签，灵活的上传下载进度监听与过程控制功能，在轻松解决很多问题的同时，设计上也力求纯粹与优雅。

* 超级优雅的 API 设计，且链式调用，让你顺滑到底！
* BaseURL、URL占位符、HTTP 同步 异步、WebSocket，让你想干啥就干啥！ 
* JSON、Xml 等自动封装与解析，且支持与任意格式的数据解析框架集成，想用啥就用啥！
* 同步拦截器、异步预处理器、回调执行器、全局监听、回调阻断 等等，让你扩展无限功能！
* 文件上传下载（过程控制、进度监听），上传下载如此简单！
* 单方法回调，充分利用 Lambda 表达式，让你代码超级简洁！
* 超级轻量，但性能卓越！

## 如何使用

### 如艺术一般优雅，像 1、2、3 一样简单

```java
// 同步 HTTP
List<User> users = OkHttps.sync("/users") 
        .get()                          // GET请求
        .getBody()                      // 响应报文体
        .toList(User.class);            // 自动反序列化 List 

// 异步 HTTP
OkHttps.async("/users/1")
        .setOnResponse(res -> {
            // 自动反序列化 Bean 
            User user = res.getBody().toBean(User.class);
        })
        .get();                         // GET请求

// WebSocket
OkHttps.webSocket("/chat") 
        .setOnMessage((WebSocket ws, Message msg) -> {
            // 从服务器接收消息
            Chat chat = msg.toBean(Chat.class);
            // 向服务器发送消息
            ws.send(chat); 
        })
        .listen();                     // 启动监听
```

### 请求三部曲

#### 第一步、确定请求方式
    
* 同步 HTTP - `sync` 方法
* 异步 HTTP - `async` 方法
* WebSocket - `webSocket` 方法

#### 第二步、构建请求任务

* `addXxxPara` - 添加请求参数
* `setOnXxxx` - 设置回调函数
* `tag` - 添加标签
* ...

#### 第三步、调用请求方法

HTTP 请求方法：

* `get()` - GET 请求
* `post()` - POST 请求
* `put()` - PUT 请求
* `delete()` - DELETE 请求
* ...

Websocket 方法：

* `listen()` - 启动监听

#### 任意网络请求，都遵循请求三部曲！

## 相关项目

项目 | 最新版本 | 描述
-|-|-
okhttps | 2.1.0 | OkHttps 核心模块
okhttps-fastjson | 2.1.0 | 与 fastjson 集成
okhttps-gson | 2.1.0 | 与 gson 集成
okhttps-jackson | 2.1.0 | 与 jackson 集成

以上是官方维护的与三大 JSON 框架集成的案例，后续将提供 xml 和 protobuf 的集成。

## 超详细教程，请查看：http://okhttps.ejlchina.com/

## 联系方式

* 微信：<img src="https://images.gitee.com/uploads/images/2020/0615/093320_429701ac_1393412.png" width="700px">
* 邮箱：zhou.xu@ejlchina.com

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
