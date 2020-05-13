---
description: OkHttps 安装 构建实例 HTTP build 同步请求 异步请求 sync async BaseUrl request webSocket gradle maven ejlchina
---

# 介绍

## OkHttps 简介

　　OkHttps 是近期开源的对 OkHttp3 轻量封装的框架，它独创的异步预处理器，特色的标签，灵活的上传下载进度监听与过程控制功能，在轻松解决很多原本另人头疼问题的同时，设计上也力求纯粹与优雅。

 * 链式调用，一点到底
 * BaseURL、URL占位符、HTTP、WebSocket
 * JSON、Xml 等自动封装与解析，且支持与任意格式的数据解析框架集成
 * 同步拦截器、异步预处理器、回调执行器、全局监听、回调阻断
 * 文件上传下载（过程控制、进度监听）
 * 单方法回调，充分利用 Lambda 表达式
 * TCP连接池、Http2

::: tip
* OkHttps 非常轻量（83Kb，Retrofit：124Kb），除 Okhttp 无第三方依赖，并且更加的开箱即用，API 也更加自然和语义化。
* OkHttps 是一个纯粹的 Java 网络开发包，并不依赖 Android，这一点和 Retrofit 不同
* OkHttps 用起来很优美，可以像 RxJava 那样链式用，却比 RxJava 更简单。
:::

### 相关项目

项目 | 最新版本 | 描述
-|-|-
okhttps-fastjson | 2.0.0.RC | 与 fastjson 集成
okhttps-gson | 2.0.0.RC | 与 gson 集成
okhttps-jackson | 2.0.0.RC | 与 jackson 集成

以上是官方维护的与三大 JSON 框架集成的案例，后续将提供 xml 和 protobuf 的集成。

## v2.x 的新特性

* HTTP 任务新增`patch()`方法，可发起 PATCH 请求，目前直接支持的 HTTP 方法有：GET、POST、PUT、PATCH、DELETE，并且暴露了`request(String method)`方法，可自定义发起任何请求，如：HEAD、OPTIONS、TRACE、CONNECT 等；

* HTTP 任务新增`skipPreproc()`和`skipSerialPreproc()`方法，具体请求可跳过 所有 或只跳过 串行 预处理器；

* 新增`MsgConvertor`接口，实现 OkHttps 与 fastjson 解耦，且不再依赖某个具体 json 框架、甚至不依赖 json，它可以与 **任何格式** 的数据解析框架集成，如：json 、xml 、protobuf 等;

* 构建`HTTP`实例时支持注入多个`MsgConvertor`，可实现同一个`HTTP`实例下，既有 json 解析，又有 xml 解析等强大特性，同时还可以让表单（form）请求参数 同 json、xml 一样，支持序列化功能。

* `HTTP`接口新增`webSocket(String url)`方法，与`sync(String url)`和`async(String url)`一样，支持 Lamda 编程、预处理器机制、消息数据自动序列化和反序列化机制；

* 新增`OkHttps`工具类，支持 SPI 方式注入配置，`OkHttps`和`HttpUtils`默认自动以 SPI 方式寻找依赖中的`MsgConvertor`；

* 新增可自定义默认编码（不自定义依然默认为 utf-8）、具体请求可指定特殊编码功能。