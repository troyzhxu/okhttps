## 介绍

　　OkHttps 是近期开源的对 OkHttp3 轻量封装的框架，它独创的异步预处理器，特色的标签，灵活的上传下载进度监听与过程控制功能，在轻松解决很多原本另人头疼问题的同时，设计上也力求纯粹与优雅。

 * 链式调用，一点到底
 * BaseURL、URL占位符、JSON自动封装与解析
 * 同步拦截器、异步预处理器、回调执行器、全局监听、回调阻断
 * 文件上传下载（过程控制、进度监听）
 * 单方法回调，充分利用 Lambda 表达式
 * TCP连接池、Http2

> 1. OkHttps 非常轻量（59Kb），是 Retrofit（124Kb）的一半，并且更加的开箱即用，API 也更加自然和语义化。
> 2. OkHttps 是一个纯粹的 Java 网络开发包，并不依赖 Android，这一点和 Retrofit 不同
> 3. OkHttps 用起来很优美，可以像 RxJava 那样链式用，却比 RxJava 更简单。


## 安装

### Maven

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps</artifactId>
     <version>1.0.5</version>
</dependency>
```
### Gradle

`implementation 'com.ejlchina:okhttps:1.0.5'`

安卓中使用需要把 JDK 版本调成 1.8，在 app 模块的 build.gradle 中加入以下配置即可：

```groovy
android {
    // 省略其它配置..
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
}
```

## 使用

### 构建实例

```java
HTTP http = HTTP.builder().build();
```
　　以上代码构建了一个最简单的`HTTP`实例，它拥有以下方法：

* `async(String url)`  开始一个异步请求 
* `sync(String url)`   开始一个同步请求 
* `cancel(String tag)` 按标签取消请求
* `cancelAll()`        取消所有HTTP任务，包括同步和异步
* `request(Request request)`  OkHttp 原生请求 
* `webSocket(Request request, WebSocketListener listener)` WebSocket通讯

　　为了使用方便，在构建的时候，我们更愿意指定一个`BaseUrl`（请参见[5.1 设置 BaseUrl](#51-设置-baseurl)）:

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")
        .build();
```
　　为了简化文档，下文中出现的`http`均是在构建时设置了`BaseUrl`的`HTTP`实例。

### 同步请求

　　使用方法`sync(String url)`开始一个同步请求：

```java
List<User> users = http.sync("/users") // http://api.demo.com/users
        .get()                         // GET请求
        .getBody()                     // 获取响应报文体
        .toList(User.class);           // 得到目标数据
```
　　方法`sync`返回一个同步`HttpTask`，可链式使用。

### 异步请求

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


### 请求方法

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

**至此，你已轻松学会了 OkHttps 95% 的常规用法！但别急，后面还有更精彩的。**