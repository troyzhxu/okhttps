---
description: OkHttps 安装 构建实例 HTTP build 同步请求 异步请求 sync async BaseUrl request webSocket gradle maven ejlchina
---

# 起步

#### 哎呀，作者还在加班中，2.x 的文档很快就出来，先看一下 1.x 的文档吧 :joy:

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

```groovy
implementation 'com.ejlchina:okhttps:1.0.5'
```

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

* `sync(String url)`   开始一个同步请求 
* `async(String url)`  开始一个异步请求 
* `cancel(String tag)` 按标签取消请求
* `cancelAll()`        取消所有HTTP任务，包括同步和异步
* `request(Request request)`  OkHttp 原生请求 
* `webSocket(Request request, WebSocketListener listener)` WebSocket通讯

　　为了使用方便，在构建的时候，我们更愿意指定一个`BaseUrl`（详见 [设置 BaseUrl](/v1/configuration.html#设置-baseurl)）:

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


**至此，你已轻松学会了 OkHttps 95% 的常规用法！但别急，后面还有更精彩的。**