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
* OkHttps 非常轻量（77Kb，Retrofit：124Kb），除 Okhttp 无第三方依赖，并且更加的开箱即用，API 也更加自然和语义化。
* OkHttps 是一个纯粹的 Java 网络开发包，并不依赖 Android，这一点和 Retrofit 不同
* OkHttps 用起来很优美，可以像 RxJava 那样链式用，却比 RxJava 更简单。
:::

### 项目组成

项目 | 最新版本 | 描述
-|-|-
[okhttps](https://gitee.com/ejlchina-zhxu/okhttps) | 2.4.1 | OkHttps 核心模块
[okhttps-fastjson](https://gitee.com/ejlchina-zhxu/okhttps/tree/master/okhttps-fastjson) | 2.4.1 | 与 fastjson 集成
[okhttps-gson](https://gitee.com/ejlchina-zhxu/okhttps/tree/master/okhttps-gson) | 2.4.1 | 与 gson 集成
[okhttps-jackson](https://gitee.com/ejlchina-zhxu/okhttps/tree/master/okhttps-jackson) | 2.4.1 | 与 jackson 集成
[okhttps-stomp](https://gitee.com/ejlchina-zhxu/okhttps-stomp) | 2.4.1 | 超级轻量的 Stomp 客户端

## v2.4 的新特性

1. 全面兼容 OkHttp 4.x 版本
2. HttpTask 新增 `getPathParas()`、`getUrlParas()`、`getBodyParas()`、`getFileParas()`、`getRequestBody()` 方法
3. 优化`HttpTask#addFilePara(String name, String type, byte[] content)`方法，添加自动生成文件名逻辑，兼容某些必须要提供文件名才能接受到文件的后端接口
4. Stomp 新增`setOnError`方法，可监听处理处理服务器发出的 ERROR 帧

## v2.3 的新特性

重新实现 WebSocket 心跳机制
使用者可以选择使用 OkHttp 自带的新桃模式，也可以选择使用 OkHttps 提供的增强型新增机制，它具有如下特性

1. 客户端发送的任何消息都具有一次客户端心跳作用
2. 服务器发送的任何消息都具有一次服务器心跳作用
3. 若服务器超过 3 * pongSeconds 秒没有回复心跳，才判断心跳超时
4. 可指定心跳的具体内容（默认为空）

## v2.2 的新特性

1. 增强泛型反序列化，支持复合泛型
2. HttpCall 接口新增 getTask 方法，可获取当前任务
3. OkHttps 工具类 新增 newBuilder 方法，可用于复制 SPI 方式注入的配置信息
4. HttpTask 类 新增 isAsyncHttp 和 isSyncHttp 方法（v2.1.0 新增了 isWebsocket 方法）可用于判断 HttpTask 的任务类型
5. 优化在 Android 端的异步请求性能

## v2.1 的新特性

* 对异步请求的响应提供了 6 种便捷回调方法，在不关心具体状态时（与全局响应监听组合使用），使用非常方便；
  - `setOnResBody` 在回调里直接取得`Body`对象，无需再使用`res.getBody()`
  - `setOnResBean` 在回调里直接取得 Java Bean 对象，无需再使用`res.getBody().toBean(Class<?>)`
  - `setOnResList` 在回调里直接取得 Java List 列表，无需再使用`res.getBody().toList(Class<?>)`
  - `setOnResMapper` 在回调里直接取得 Mapper 对象，无需再使用`res.getBody().toMapper()`
  - `setOnResArray` 在回调里直接取得 Array 对象，无需再使用`res.getBody().toArray()`
  - `setOnResString` 在回调里直接取得 String 对象，无需再使用`res.getBody().toString()`

* 优化性能：使用`HTTP#newBuilder()`方法克隆`HTTP`实例时，新实例与旧实例之间资源共享最大化。


## v2.0 的新特性

* HTTP 任务新增`patch()`和`head()`方法，可发起 PATCH 和 HEAD 请求，目前直接支持的 HTTP 方法有：GET、POST、PUT、PATCH、DELETE，并且暴露了`request(String method)`方法，可自定义发起任何请求，如：HEAD、OPTIONS、TRACE、CONNECT 等；

* HTTP 任务新增`skipPreproc()`和`skipSerialPreproc()`方法，具体请求可跳过 所有 或只跳过 串行 预处理器；

* 新增`MsgConvertor`接口，实现 OkHttps 与 fastjson 解耦，且不再依赖某个具体 json 框架、甚至不依赖 json，它可以与 **任何格式** 的数据解析框架集成，如：json 、xml 、protobuf 等;

* 构建`HTTP`实例时支持注入多个`MsgConvertor`，可实现同一个`HTTP`实例下，既有 json 解析，又有 xml 解析等强大特性，同时还可以让表单（form）请求参数 同 json、xml 一样，支持序列化功能。

* `HTTP`接口新增`webSocket(String url)`方法，与`sync(String url)`和`async(String url)`一样，支持 Lamda 编程、预处理器机制、消息数据自动序列化和反序列化机制；

* 新增`OkHttps`工具类，支持 SPI 方式注入配置，`OkHttps`和`HttpUtils`默认自动以 SPI 方式寻找依赖中的`MsgConvertor`；

* 新增可自定义默认编码（不自定义依然默认为 utf-8）、具体请求可指定特殊编码功能。

<br/>

<Vssue :title="$title" />
