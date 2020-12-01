---
description: OkHttps 安装 构建实例 HTTP build 同步请求 异步请求 sync async BaseUrl request webSocket gradle maven ejlchina
---

# 起步

## 安装

### Maven

#### 单独使用 OkHttps

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps</artifactId>
     <version>2.4.5</version>
</dependency>
```

单独使用 OkHttps 需要自定义[`MsgConvertor`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/MsgConvertor.java)，否则无法使用 **自动正反序列化** 相关功能，后文会详细讲解哪些功能会受到此影响。

#### 与 fastjson 一起使用

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps-fastjson</artifactId>
     <version>2.4.5</version>
</dependency>
```

#### 与 gson 一起使用

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps-gson</artifactId>
     <version>2.4.5</version>
</dependency>
```

#### 与 jackson 一起使用

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps-jackson</artifactId>
     <version>2.4.5</version>
</dependency>
```

::: tip
以上依赖四选一即可
:::

#### 集成 XML 扩展（since v2.4.2）

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps-xml</artifactId>
     <version>2.4.5</version>
</dependency>
```

#### 使用 Stomp 客户端

```xml
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>okhttps-stomp</artifactId>
     <version>2.4.5</version>
</dependency>
```

### Gradle

#### 单独使用 OkHttps

```groovy
implementation 'com.ejlchina:okhttps:2.4.5'
```

单独使用 OkHttps 需要自定义[`MsgConvertor`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/MsgConvertor.java)，否则无法使用 **自动正反序列化** 相关功能，后文会详细讲解哪些功能会受到此影响。

#### 与 fastjson 一起使用

```groovy
implementation 'com.ejlchina:okhttps-fastjson:2.4.5'
```

#### 与 gson 一起使用

```groovy
implementation 'com.ejlchina:okhttps-gson:2.4.5'
```

#### 与 jackson 一起使用

```groovy
implementation 'com.ejlchina:okhttps-jackson:2.4.5'
```

::: tip
以上依赖四选一即可
:::

#### 集成 XML 扩展（since v2.4.2）

```groovy
implementation 'com.ejlchina:okhttps-xml:2.4.5'
```

#### 使用 Stomp 客户端

```groovy
implementation 'com.ejlchina:okhttps-stomp:2.4.5'
```

#### JDK 版本

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

### 构建 HTTP

```java
HTTP http = HTTP.builder().build();
```

以上代码构建了一个最简单的`HTTP`实例，它拥有以下方法：

* `sync(String url)`   开始一个同步 HTTP 请求 
* `async(String url)`  开始一个异步 HTTP 请求 
* `webSocket(String url)`  开始一个 WebSocket 连接 
* `cancel(String tag)` 按标签取消（同步 | 异步 | WebSocket）连接
* `cancelAll()`        取消所有（同步 | 异步 | WebSocket）连接
* `request(Request request)`  OkHttp 原生 HTTP 请求 
* `webSocket(Request request, WebSocketListener listener)` OkHttp 原生 WebSocket 连接
* `newBuilder()`       用于重新构建一个 HTTP 实例

为了使用方便，在构建的时候，我们更愿意指定一个`BaseUrl`和[`MsgConvertor`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/MsgConvertor.java)（详见 [设置 BaseUrl](/v1/configuration.html#设置-baseurl) 和 [消息转换器](/v2/configuration.html#消息转换器)）:

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.example.com")
        .addMsgConvertor(new GsonMsgConvertor())
        .build();
```

上例中的[`GsonMsgConvertor`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps-gson/src/main/java/com/ejlchina/okhttps/GsonMsgConvertor.java)来自[`okhttps-gson`](https://gitee.com/ejlchina-zhxu/okhttps/tree/master/okhttps-gson)。为了简化文档，下文中出现的`http`均是在构建时设置了`BaseUrl`和`MsgConvertor`的`HTTP`实例。

### 同步请求

　　使用方法`sync(String url)`开始一个同步请求：

```java
List<User> users = http.sync("/users") // http://api.example.com/users
        .get()                         // GET请求
        .getBody()                     // 获取响应报文体
        .toList(User.class);           // 得到目标数据
```

　　方法`sync`返回一个同步`HttpTask`，可链式使用。

### 异步请求

　　使用方法`async(String url)`开始一个异步请求：

```java
http.async("/users/1")                //  http://api.example.com/users/1
        .setOnResponse((HttpResult res) -> {
            // 得到目标数据
            User user = res.getBody().toBean(User.class);
        })
        .get();                       // GET请求
```
　　方法`async`返回一个异步`HttpTask`，可链式使用。

### WebSocket

　　使用方法`webSocket(String url)`开始一个 WebSocket 通讯：

```java
http.webSocket("/chat") 
        .setOnOpen((WebSocket ws, HttpResult res) -> {
            ws.send("向服务器问好");
        })
        .setOnMessage((WebSocket ws，Message msg) -> {
            // 从服务器接收消息（自动反序列化）
            Chat chat = msg.toBean(Chat.class);
            // 相同的消息发送给服务器（自动序列化 Chat 对象）
            ws.send(chat); 
        })
        .listen();                     // 启动监听
```
　　方法`webSocket`返回一个支持 WebSocket 的`HttpTask`，也可链式使用。

### 请求三步曲

#### 第一步、确定请求方式
    
同步 HTTP（`sync`）、异步 HTTP（`async`）或 WebSocket（`webSocket`）

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

#### 任意请求，都遵循请求三部曲！

### 工具类

OkHttps 提供了两个开箱即用的工具类，让你从此告别封装工具类的烦恼：

工具类 | 起始版本 | 功能特点
-|-|-
`OkHttps` | `2.0.0.RC` | 支持自动注入`MsgConvertor`，支持 [SPI 方式注入配置](#配置okhttps)，推荐用于主应用中的网络开发
`HttpUtils` | `1.0.0` | 自`2.0.0.RC`开始支持自动注入`MsgConvertor`，不建议再做其它配置，推荐用于第三方依赖包中的网络开发

这两个工具都类会自动以 SPI 方式注入依赖中的`MsgConvertor`（在第一次使用的时候），只要你的项目中添加了相关依赖（如：okhttps-fastjson 等）。并且它们和`HTTP`接口拥有几乎一样的方法，并且这些方法都是静态的：

* `sync(String url)`   开始一个同步 HTTP 请求 
* `async(String url)`  开始一个异步 HTTP 请求 
* `webSocket(String url)`  开始一个 WebSocket 连接 
* `cancel(String tag)` 按标签取消（同步 | 异步 | WebSocket）连接
* `cancelAll()`        取消所有（同步 | 异步 | WebSocket）连接
* `request(Request request)`  OkHttp 原生 HTTP 请求 
* `webSocket(Request request, WebSocketListener listener)` OkHttp 原生 WebSocket 连接

例如，使用`OkHttps`，无需考虑构造`HTTP`实例，你可以这样发起一个请求：

```java
OkHttps.async("https://api.example.com/auth/login")
        .addBodyPara("username", "jack")
        .addBodyPara("password", "xxxx")
        .setOnResponse((HttpResult result) -> {
            // 得到返回数据，使用 Mapper 可省去定义一个实体类
            Mapper mapper = result.getBody().toMapper();
            // 登录是否成功
            boolean success = mapper.getBool("success");
        })
        .post();
```

### 配置`OkHttps`

工具类`OkHttps`还支持以 SPI 方式注入自定义配置，分以下两步：

#### 第一步、新建一个配置类，实现[`com.ejlchina.okhttps.Config`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/Config.java)接口

例如：

```java
package com.example.okhttps;

import com.ejlchina.okhttps.Config;
import com.ejlchina.okhttps.HTTP;

public class OkHttpsConfig implements Config {

    @Override
    public void with(HTTP.Builder builder) {
        // 在这里对 HTTP.Builder 做一些自定义的配置
        builder.baseUrl("https://api.domo.com");
        // 如果项目中添加了 okhttps-fastjson 或 okhttps-gson 或 okhttps-jackson 依赖
        // OkHttps 会自动注入它们提供的 MsgConvertor 
        // 所以这里就不需要再配置 MsgConvertor 了 (内部实现自动注入的原理也是 SPI)
        // 但如果没有添加这些依赖，那还需要自定义一个 MsgConvertor
        builder.addMsgConvertor(new MyMsgConvertor());
    }

}
```

以上做简单的演示，更多配置案例可参考 [安卓-最佳实践](/v2/android.html#最佳实践) 章节。

#### 第二步、在项目的`/src/main`目录下新建`resources/META-INF/services/com.ejlchina.okhttps.Config`文件，文件内容是上一步自定义的配置类的全名

例如：

![](/spi_config.png)

以上两步就完成了对`OkHttps`工具类的自定义配置，接下来你便可以体验它神奇的能力了：

```java
List<User> users = OkHttps.sync("/users") // http://api.example.com/users
        .get().getBody().toList(User.class);
```

#### 为什么要这样呢？

有同学可能会疑问，为什么要这样配置呢，不能直接给`OkHttps`设置一个`HTTP`实例吗，像这样：

```java
HTTP http = HTTP.builder()
        // 自定义配置...
        .build();

OkHttps.set(http);
```

这样做在一般情况下确实可以，但是在某些 Java 虚拟机上（特别在 Android）中，有一个致命的缺陷：那就是系统在某些情况下会回收静态变量，而`OkHttps`内部持有的`HTTP`实例一旦被回收后，上述方式自定义的配置就会丢失，这样在后续的逻辑处理中就可能会造成严重的问题！

而 SPI 注入的方式则可以很好的解决这个问题：即使`OkHttps`内部的`HTTP`实例被回收，在下次使用时，`OkHttps`又会自动加载注入的配置重新构建一个`HTTP`实例，这样就保证了用户自定义的配置不会丢失。

所以我们推荐在 Android 应用开发中，直接使用`OkHttps`，自定义配置通过 SPI 方式注入。

而在开发具有网络请求需求的依赖项目时，直接使用`HttpUtils`并且不做自定义的配置，如果一定要自定义配置，那就使用`HTTP.Builder`重新构建一个`HTTP`实例使用吧。

### IDEA 小技巧

由于 OkHttps 遵循请求三部曲，所以我们在 IDEA 中设置一个代码模板（Android Studio 一样设置）

* 菜单 File -> Settings 打开 Settings 设置框
* 展开 Editor -> Live Templates

![](/code_temp.png)

如图设置好后，写代码时，输入`oks`，按回车键，就会自动生成模版，赶快尝试一下吧。


**至此，你已轻松学会了 OkHttps 90% 的常规用法！但别急，后面还有更加精彩的。**

<br/>

<Vssue :title="$title" />