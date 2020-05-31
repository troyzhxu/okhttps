---
description: OkHttps 请求方法 回调函数 HttpResult cache 多次 消费报文体 HttpCall 构建HTTP任务 请求参数 JSON 使用标签 发起请求 取消请求
---

# 基础

## 请求方法

OkHttps 使用`sync()`和`async()`方式发起的请求，支持的 HTTP 方法有：

HTTP 请求方法 | 实现方法 | Restful 释义 | 起始版本
-|-|-|-
GET | `get()` | 获取资源 | 1.0.0
HEAD | `head()` | 获取资源头信息 | 2.0.0
POST | `post()` | 提交资源 | 1.0.0
PUT | `put()` | 更新资源 | 1.0.0
PATCH | `patch()` | 部分更新资源 | 2.0.0.RC
DELETE | `delete()` | 删除资源 | 1.0.0
其它 | `request(String method)` | 任何 HTTP 请求方法 | 2.0.0.RC

例如，修改 ID 为 100 的书的标题，可以这样请求：

```java
http.async("/books/100")
        .addBodyPara("title", "新标题")
        .patch();                   // 发起 PATCH 请求
```

再如，删除 ID 为 100 的书，可以发起这样的请求：

```java
http.async("/books/100").delete();  // 发起 DELETE 请求
```

再如，使用 HEAD 请求获取服务器上文件的大小：

```java
HttpResult res = http.sync("/download/file.zip").head();
// // HttpResult#getContentLength() 新增于 2.0.0 版本
System.out.println("size = " + res.getContentLength());
```

你还可以自定义一些请求方法，例如：

```java
http.async("/somethings")
        .setOnResponse((HttpResult result) -> {
            System.out.println(result);
        })
        .request("OPTIONS");        // 发起 OPTIONS 请求
```

另外，同步请求的所有这些方法都会返回一个[`HttpResult`](#httpresult)，而异步请求则会返回一个[`HttpCall`](#httpcall)：

```java
HttpResult res1 = http.sync("/books").get();     // 同步 GET
HttpResult res2 = http.sync("/books").post();    // 同步 POST

HttpCall call1 = http.async("/books").get();     // 异步 GET
HttpCall call2 = http.async("/books").post();    // 异步 POST
```

## 回调函数

OkHttps 的回调函数全部使用单方法模式，这样可以充分利用 Java8 或 Kotlin 中的 Lambda 表达式，使你的代码更加简洁优雅：

### 普通回调

```java
http.async("/users")        // http://api.demo.com/users
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        })
        .setOnException((IOException e) -> {
            // 异常回调
        })
        .setOnComplete((State state) -> {
            // 完成回调，无论成功失败都会执行，并且在 响应|异常回调 之前执行
            // 可以根据 state 枚举判断执行状态:
            // State.CANCELED`      请求被取消
            // State.RESPONSED`     已收到响应
            // State.TIMEOUT`       请求超时
            // State.NETWORK_ERROR` 网络错误
            // State.EXCEPTION`     其它请求异常
        })
        .get();
```

OkHttps 同时还支持 **全局回调** 和 **回调阻断** 机制，详见 [全局回调监听](/v2/configuration.html#全局回调监听)。

::: tip
* 只有异步请求才可以设置这三种（响应|异常|完成）回调
* 同步请求直接返回结果，无需使用回调
:::

### 进度回调

上传进度回调：

```java
http.sync("/upload")
        .addFilePara("avatar", "D:/image/avatar.jpg")
        .setOnProcess((Process process) -> { 
            long doneBytes = process.getDoneBytes();   // 已上传字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已上传的比例
            boolean isDone = process.isDone();         // 是否上传完成
        })
        .post();
```

关于上传的更多细节请参考 [文件上传](/v2/updown.html#文件上传) 章节。

下载进度回调：

```java
http.sync("/download/test.zip").get().getBody()
        .setOnProcess((Process process) -> {
            long doneBytes = process.getDoneBytes();   // 已下载字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已下载的比例
            boolean isDone = process.isDone();         // 是否下载完成
        })
        .toFolder("D:/download/")        // 指定下载的目录，文件名将根据下载信息自动生成
        .start();
```

#### 还有下载成功或失败的回调：

```java
http.sync("/download/test.zip").get().getBody()
        .toFile("D:/download/test.zip")              // 下载到文件
        .setOnSuccess((File file) -> {
            // 下载成功
        })
        .setOnFailure((Failure failure) -> {
            // 下载失败
        })
        .start();
```

关于下载的更多内容，请参考 [文件下载](/v2/updown.html#文件下载) 章节。

### WebSocket 回调

```java
http.webSocket("/websocket-endpoint")
        .setOnOpen((WebSocket ws, HttpResult res) -> {
            // WebSocket 连接建立回调
        })
        .setOnMessage((WebSocket ws, Message msg) -> {
            // 服务器下发消息回调
        })
        .setOnException((WebSocket ws, Throwable thr) -> {
            // 连接发生异常回调
        })
        .setOnClosing((WebSocket ws, WebSocket.Close close) -> {
            // 连接正在关闭回调
        })
        .setOnClosed((WebSocket ws, WebSocket.Close close) -> {
            // 连接已关闭（v2.0.0 之后包含连接被取消 和 连接发生异常）
            boolean isCanceled = close.isCanceled();    // from v2.0.0
            boolean isException = close.isException();  // from v2.0.0
            int code = close.getCode();
            String reason = close.getReason();
        })
        .listen();
```

::: tip
OkHttps 的所有回调函数都默认在 IO 线程执行，若要切换默认线程（例如 Android 中切换到 UI 线程）请参见 [回调线程切换](/v2/android.html#回调线程切换)。
:::

## HttpResult

　　`HttpResult`是HTTP请求执行完后的结果，它是同步请求方法`get()`、`post()`等的返回值，也是异步请求响应回调`OnResponse`的参数，它定义了如下方法：

* `getState()`         得到请求执行状态枚举，它有以下取值：
    * `State.CANCELED`      请求被取消
    * `State.RESPONSED`     已收到响应
    * `State.TIMEOUT`       请求超时
    * `State.NETWORK_ERROR` 网络错误
    * `State.EXCEPTION`     其它请求异常
* `getStatus()`        得到 HTTP 状态码
* `isSuccessful()`     是否响应成功，状态码在 [200..300) 之间
* `getHeaders()`       得到HTTP响应头
* `getHeaders(String name)` 得到HTTP响应头
* `getHeader(String name)`  得到HTTP响应头
* `getContentLength();` 解析响应头中的 Content-Length 长度信息（v2.0.0 新增）
* `getBody()`          得到响应报文体`Body`实例，它定义了如下方法（对同一个`Body`实例，以下的`toXXX()`类方法只能使用一个且仅能调用一次，除非先使用 cache 方法）：
    * `toBytes()`                报文体转换为字节数组
    * `toByteStream()`           报文体转换为字节输入流
    * `toCharStream()`           报文体转换为字符输入流
    * `toString()`               报文体转换为字符串文本
    * `toBean(Class<T> type)`    报文体根据`type`自动反序列化为 JavaBean（依赖`MsgConvertor`）
    * `toList(Class<T> type)`    报文体根据`type`自动反序列化为 JavaBean 列表（依赖`MsgConvertor`）
    * `toMapper()`               报文体自动反序列化为映射结构的对象（依赖`MsgConvertor`，v2.0.0.RC 之前是`toJsonObject()`）
    * `toArray()`                报文体自动反序列化为数组结构的对象（依赖`MsgConvertor`，v2.0.0.RC 之前是`toJsonArray()`）
    * `toFile(String filePath)`  下载到指定路径
    * `toFile(File file)`        下载到指定文件
    * `toFolder(String dirPath)` 下载到指定目录
    * `toFolder(File dir)`       下载到指定目录
    * `getType()`                返回报文体的媒体类型（v2.0.0 新增，不再推荐`getContentType()`方法）
    * `getLength()`              返回报文体的字节长度（v2.0.0 新增，不再推荐`getContentLength()`方法）
    * `cache()`                  缓存报文体，开启缓存后可重复使用`toXXX()`类方法
    * `close()`                  关闭报文体，未对报文体做任何消费时使用，比如只读取报文头
* `getError()`         执行中发生的异常，自动捕获执行请求时发生的 网络超时、网络错误 和 其它IO异常
* `close()`            关闭报文，未对报文体做任何消费时使用，比如只读取长度

示例，请求结果自动反序列化为 Bean 和 List：

```java
// 自动转Bean
Order order = http.sync("/orders/1").get()
        .getBody().toBean(Order.class);

// 自动转List
List<Order> orders = http.sync("/orders")
        .get().getBody().toList(Order.class);
```

上例中，为了获取订单列表，必须先定义一个`Order`类，如果你不想定义它，可以直接使用`toMapper()`和`toArray()`方法：

```java
Mapper order = http.sync("/orders/1")
        .get().getBody().toMapper();

long orderId = order.getLong("id");         // 订单ID
long orderNo = order.getString("orderNo");  // 订单号

Array orders = http.sync("/orders")
        .get().getBody().toArray();

int size = orders.size();                   // 订单个数
Mapper o1 = orders.getMapper(0)             // 第1个订单
```

关于 Mapper 和 Array 的更多信息，请参考 [Mapper 接口](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/Mapper.java) 和 [Array 接口](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/Array.java)。

示例，使用 `cache()` 方法：

```java
Body body = http.sync("/orders").get().getBody().cache();

// 使用 cache 后，可以多次使用 toXXX() 方法
System.out.println(body.toString());
System.out.println(body.toArray());
System.out.println(body.toList(Order.class));
```

示例，获取下载文件的大小：

```java
long size = http.sync("/download/test.zip").get().getBody()
            .close()        // 只是想获得文件大小，不消费报文体，所以直接关闭
            .getLength();   // 获得待下载文件的大小
// 由于未消费报文体，所以本次请求不会消耗下载报文体的时间和网络流量
System.out.println("size = " + size);
```

上述代码等同于：

```java
long size = http.sync("/download/test.zip").get().close().getContentLength(); 
System.out.println("size = " + size);
```

还等效于：

```java
long size = http.sync("/download/test.zip").head().getContentLength(); 
// 因为 HEAD 请求没有响应报文体，所以就不需要关闭啦
System.out.println("size = " + size);
```

## HttpCall

[`HttpCall`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/HttpCall.java)是异步请求方法`get()`、`post()`等的返回值，它是一个接口，继承自[`Cancelable`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/Cancelable.java)，与`java`的`Future`接口很像，它有如下方法：

* `cancel()` 取消本次请求，返回取消结果
* `isCanceled()` 返回请求是否被取消
* `isDone()` 返回是否执行完成，包含取消和失败
* `getResult()` 返回执行结果`HttpResult`对象，若请求未执行完，则挂起当前线程直到执行完成再返回

例如，取消一个异步请求：

```java
HttpCall call = http.async("/users/1").get();

System.out.println(call.isCanceled());     // false

boolean success = call.cancel();           // 取消请求

System.out.println(success);               // true
System.out.println(call.isCanceled());     // true
```

## 构建请求

　　`HTTP`接口的`sync(String url)`、`async(String url)`和`webSocket(String url)`方法返回一个`HttpTask`对象，它实现了[`Cancelable`](https://gitee.com/ejlchina-zhxu/okhttps/blob/master/okhttps/src/main/java/com/ejlchina/okhttps/Cancelable.java)接口，同时也提供了可链式调用的`addXXX`与`setXXX`等系列方法用于构建请求报文。

### 请求头

* `addHeader(String name, String value)`    添加请求头
* `addHeader(Map<String, String> headers)`  添加请求头

### 路径参数

* `addPathPara(String name, Object value)` 添加路径参数：替换 URL 里的`{name}`占位符
* `addPathPara(Map<String, ?> params)`     添加路径参数：替换 URL 里的`{name}`占位符

路径参数可以存在于 URL 中的任何位置，例如：

```java
final String BOOKS_QUERY_URL = "/authors/{authorId}/books?type={type}";

http.async(BOOKS_QUERY_URL)     // /authors/1/books?bookType=2
        .addPathPara("authorId", 1)
        .addPathPara("type", 2)
        .setOnResponse((HttpResult res) -> {

        })
        .get();
```

### 查询参数

* `addUrlPara(String name, Object value)`  添加 URL 参数：拼接在 URL 的`?`之后（查询参数）
* `addUrlPara(Map<String, ?> params)`      添加 URL 参数：拼接在 URL 的`?`之后（查询参数）

::: warning
方法`addXxxPara`在 v2.0.0.RC 之前名为`addXxxParam`, v2.0.0.RC 之后推荐使用简洁版的方法，老方法将在 v2.1.0 中移除。
:::

### 报文体类型

* `bodyType(String type)` 结合报文体参数使用，用于设置本次的请求报文体类型，可以是`form`、`json`、`xml`、`protobuf`等，默认为`form`（表单类型）,可以在构建`HTTP`实例时修改默认的类型

::: tip 解析
`bodyType`对应于`MsgConvertor`的`mediaType()`方法的返回值，OkHttps 会按照既定的规则 [`mediaType().contain(bodyType)`] 去匹配出对应的`MsgConvertor`去序列化报文体参数。
:::

### 报文体参数（表单、JSON、XML等）

OkHttps 信仰统一与一致更加优雅，所以自 v2.0.0.RC 开始、它便统一了报文体参数的 API，无论是表单、还是 JSON、XML 或是 protobuf 等等，只要这些参数是放在请求报文体中，那么就可以通过一套 API 轻松搞定！

* `addBodyPara(String name, Object value)` 添加 Body 参数
* `addBodyPara(Map<String, ?> params)`     添加 Body 参数
* `setBodyPara(Object object)`             设置 Body 参数体  

::: warning 注意
添加报文体参数后，不可使用 GET、HEAD 请求方法

方法`setBodyPara`在 v2.0.0.RC 之前名为`setRequestJson`, v2.0.0.RC 之后推荐使用统一 API，老方法将在 v2.1.0 中移除。
:::

#### 表单请求

默认的请求报文体类型就是`form`表单，所以可以直接使用`addBodyPara`方法：

```java
http.async("/projects") 
        .addBodyPara("name", "OkHttps")
        .addBodyPara("desc", "最好用的网络框架")
        .post();
```

或者是传入一个 Map 对象：

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "OkHttps");
params.put("desc", "最好用的网络框架");

http.async("/projects")
        .addBodyPara(params)
        .post();  
```

甚至可以用`setBodyPara`传入一个`String`：

```java
http.async("/projects") 
        .setBodyPara("name=OkHttps&desc=最好用的网络框架")
        .post();  
```

如果你配置了`FormConvertor`（请参考 [配置-表单序列化](/v2/configuration.html#表单序列化) 章节），那你还可以直接传入一个 POJO（自定义的一个 Java 类）：

```java
Proejct project = new Proejct();
project.setName("OkHttps");
project.setDesc("最好用的网络框架");

http.async("/projects") 
        .setBodyPara(project)       // 将自动序列化为表单格式
        .post();
```

以上 **4** 种方式具有相同的效果，但如果你修改了默认的报文体序列化类型（请参考 [默认序列化类型](/v2/configuration.html#默认序列化类型) 章节），那还需在请求时指定当前的请求报文体类型：

```java
http.async("/projects") 
        .bodyType("form")           // 指明请求体类型是表单
        ...
```

或使用`OkHttps.FORM`常量

```java
http.async("/projects") 
        .bodyType(OkHttps.FORM)     // 指明请求体类型是表单
        ...
```

#### JSON 请求

JSON 请求要求默认的请求`bodyType`为`json` 或者 在具体请求中显式指明`bodyType`为`json`，其它用法和表单请求一模一样。

单个添加

```java
http.async("/projects") 
        .addBodyPara("name", "OkHttps")
        .addBodyPara("desc", "最好用的网络框架")
        .post();
```

Map 方式：

```java
Map<String, Object> params = new HashMap<>();
params.put("name", "OkHttps");
params.put("desc", "最好用的网络框架");

http.async("/projects") 
        .addBodyPara(params)
        .post();  
```

POJO 方式：

```java
Proejct project = new Proejct();
project.setName("OkHttps");
project.setDesc("最好用的网络框架");

http.async("/projects") 
        .setBodyPara(project)       // 自动序列化
        .post();   
```

字符串方式：

```java
http.async("/projects") 
        .setBodyPara("{\"name\":\"OkHttps\",\"desc\":\"最好用的网络框架\"}")
        .post();  
```

唯一的不同是，如果默认的`bodyType`不是`json`，那需要显式指定当前请求的`bodyType`为`json`：

```java
http.async("/projects") 
        .bodyType("json")           // 指明请求体类型是 JSON
        ...
```

或使用`OkHttps.JSON`常量

```java
http.async("/projects") 
        .bodyType(OkHttps.JSON)     // 指明请求体类型是 JSON
        ...
```

#### XML 请求

若默认不是 XML，则显式指定当前请求的报文体类型

```java
http.async("/projects") 
        .bodyType("xml")           // 指明请求体类型是 XML
        ...
```

或使用`OkHttps.XML`常量

```java
http.async("/projects") 
        .bodyType(OkHttps.XML)     // 指明请求体类型是 XML
        ...
```

其它与 表单和 JSON，完全一致，不再赘述。

#### 其它数据格式的请求

只需要扩展了相应的`MsgConvertor`，OkHttps 支持任意数据格式的请求。

### 文件参数

文件参数是一个特殊的报文体参数，当使用以下方法上传文件时，OkHttps 会强制报文体使用`form`模式。

* `addFilePara(String name, String filePath)` 上传文件
* `addFilePara(String name, File file)` 上传文件
* `addFilePara(String name, String type, byte[] content)` 上传文件
* `addFilePara(String name, String type, String fileName, byte[] content)` 上传文件

文件参数和报文体参数，还可以混用，例如：

```java
http.sync("/upload")
        .addBodyPara("title", "头像")
        .addFilePara("image", "D:/image/avatar.jpg")
        .post();
```

::: warning 
如果你使用的是 v2.0.0.RC 版本中，以上代码会报 **“方法 addFilePara 只能使用 form 方式请求”** 错误，可以用以下方式解决该问题：
```java
http.async("/upload")
        .bodyType("multipart/form")
        .addBodyPara("title", "头像")
        .addFilePara("test", "D:/image/avatar.jpg")
        .post();
```
详见 ISSUE: [https://gitee.com/ejlchina-zhxu/okhttps/issues/I1H8G9](https://gitee.com/ejlchina-zhxu/okhttps/issues/I1H8G9)
:::

无论当前的默认`bodyType`是什么，和文件参数一起添加的`Body`参数都将以`form`（表单）模式提交。

### 其它

* `tag(String tag)` 为 HTTP 任务添加标签
* `setRange(long rangeStart)` 设置 Range 头信息，`addHeader`的便捷方法，用于[断点续传](/v1/updown.html#实现断点续传)
* `setRange(long rangeStart, long rangeEnd)` 设置 Range 头信息，可用于[分块下载](/v1/updown.html#实现分块下载)
* `bind(Object object)` 绑定一个对象，可用于实现 Android 里的[生命周期绑定](/v1/android.html#生命周期绑定)
* `nothrow()` 一种发生异常时的处理方式，请参考 [异常处理](/v2/features.html#异常处理) 章节
* `skipPreproc()` 设置本次请求跳过所有预处理器，请参考 [预处理器](/v2/configuration.html#并行预处理器) 章节
* `skipSerialPreproc()` 设置本次请求只跳过所有串行预处理器，请参考 [串行预处理器](/v2/configuration.html#串行预处理器（token问题最佳解决方案）) 章节

## 使用标签

有时候我们想对 HTTP 任务加以分类，这时候可以使用标签功能：

```java
http.async("/users")    //（1）
        .tag("A")
        .get();
        
http.async("/users")    //（2）
        .tag("A.B")
        .get();
        
http.async("/users")    //（3）
        .tag("B")
        .get();
        
http.async("/users")    //（4）
        .tag("B")
        .tag("C")       // 从 v1.0.4 标签将以追加模式添加，等效于 setTag("B.C")
        .get();
        
http.async("/users")    //（5）
        .tag("C")
        .get();

http.webSocket("/websocket")
        .tag("B")       // (6) 标签同样可以作用在 WebSocket 连接上
        .listen();
```

### 预处理器中访问标签

在 [预处理器](/v2/configuration.html#并行预处理器) 中可以通过`chain`对象获得当前的请求任务`HttpTask`，然后使用`isTagged`或`getTag`方法获取标签信息：

```java
HTTP http = HTTP.builder()
        // 添加预处理器
        .addPreprocessor(chain -> {

            // 获得当前的HTTP任务
            HttpTask<?> task = chain.getTask();
            // 判断该任务是否添加了 "A" 标签
            boolean tagged = task.isTagged("A");
            // 得到整个标签串
            String tag = task.getTag();
            
            // ...
        })
        // ...
```

在 [串行预处理器](/v2/configuration.html#串行预处理器（token问题最佳解决方案）) 中也是同样的方法访问标签。

### 全局监听中访问标签

在 [全局监听](/v2/configuration.html#全局监听) 中，访问标签就加简单一点，因为`HttpTask`正是形参之一，例如全局响应监听：

```java
HTTP http = HTTP.builder()
        // 全局响应监听
        .responseListener((HttpTask<?> task, HttpResult result) -> {

            // 判断该任务是否添加了 "A" 标签
            boolean tagged = task.isTagged("A");
            // 得到整个标签串
            String tag = task.getTag();

            // ...
        })
        // ...
```

其它类型的全局监听也是同样的方法访问标签。

### 拦截器中访问标签

自 v2.0.1 起，OkHttps 支持在拦截器内访问标签，可通过`Request`对象拿到整个标签串：

```java
HTTP http = HTTP.builder()
        // OkHttpClient 原生配置
        .config(b -> {
            // 添加拦截器
            b.addInterceptor(chain -> {

                // 拿到 Request 对象
                Request request = chain.request();
                // 拿到整个标签串
                String tag = request.tag(String.class);
                
                // ...
            });
        })
        // ...
```

拦截器的配置，可参见 [配置 OkHttpClient](/v2/configuration.html#配置-okhttpclient) 章节。

### 使用标签取消请求

当使用标签后，还可以按标签批量的对HTTP任务进行取消：

```java
int count = http.cancel("B");  //（2）（3）（4）（6）被取消（取消标签包含"B"的任务）
System.out.println(count);     // 输出 4
```

取消请求任务，只是标签的一个附带功能。

标签 真正的强大之处在于：它可以和 [预处理器](/v2/configuration.html#并行预处理器) 和 [全局监听](/v2/configuration.html#全局监听) 及 拦截器 配合使用，以此来扩展很多功能。可参考 [串行预处理器（token问题最佳解决方案）](/v2/configuration.html#串行预处理器（token问题最佳解决方案）)和 [安卓-自动加载框](/v2/android.html#自动加载框) 等章节。

另外，请求任务的取消，还有更多的方式，可参考 [取消请求](/v2/foundation.html#取消请求) 章节

## 异常处理

　　使用 OkHttps 时，**异常处理不是必须的**，但相比其它的 HTTP 开发包，它还提供一个特别的处理方法：`nothrow()`，以满足不同的异常处理需求。

### 同步请求的异常

　　默认情况下，当同步请求执行异常时，会直接向外抛出，我们可以用 `try catch` 来捕获，例如：

```java
try {
    HttpResult result = http.sync("/users/1").get();
} catch (HttpException e) {
    Throwable cause = e.getCause(); // 得到异常原因
    if (cause instanceof ConnectException) {
        // 当没网络时，会抛出连接异常
    }
    if (cause instanceof SocketTimeoutException) {
        // 当接口长时间未响应，会抛出超时异常
    }
    if (cause instanceof UnknownHostException) {
        // 当把域名或IP写错，会抛出 UnknownHost 异常
    }
    // ...
}
``` 
　　这种传统的异常处理方式，当然可以解决问题，但 OkHttps 有更佳的方案：

```java
// 方法  nothrow() 让异常不直接抛出
HttpResult result = http.sync("/users/1").nothrow().get();
// 判断执行状态
switch (result.getState()) {
    case RESPONSED:     // 请求已正常响应
        break;
    case CANCELED:      // 请求已被取消
        break;
    case NETWORK_ERROR: // 网络错误，说明用户没网了
        break;
    case TIMEOUT:       // 请求超时
        break;
    case EXCEPTION:     // 其它异常
        break;
}
// 还可以获得具体的异常信息
IOException error = result.getError();
``` 

### 异步请求的异常

　　异步请求最常用的异常处理方式就是设置一个异常回调：

```java
http.async("/users/1")
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .setOnException((IOException e) -> {
            // 这里处理请求异常
        })
        .get();
```
　　当然，还有一个全局异常监听（`ExceptionListener`，请参考 [全局回调监听](/v2/configuration.html#全局回调监听) 章节）：

```java
HTTP http = HTTP.builder()
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有请求执行完都会走这里

            // 返回 true 表示继续执行 task 的 OnComplete 回调，
            // 返回 false 则表示不再执行，即 阻断
            return true;
        })
        .build();
```
　　如果不设置`OnException`回调，也没有`ExceptionListener`，发生异常时会在 **IO 线程** 中向上抛出，外层无法捕获：

```java
try {
    http.async("/users/1")
            .setOnResponse((HttpResult result) -> {
                // 当发生异常时就不会走这里
            })
            .get();
} catch (HttpException e) {
    // 这种方式是捕获不到异常的！！！！！！
}
```
　　即使没有`OnException`回调，发生异常时，依然会走`OnComplete`回调，如果设置了的话：

```java
http.async("/users/1")
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .setOnComplete((State state) -> {
            // 发生异常，会先执行这里，可以根据 state 判断发生了什么
            // 但执行完后依然会在IO线程中向上抛出
        })
        .get();
```
　　如果就是想 **不处理异常，也不向上抛出**，发生错误完全无视，可以做到吗？可以！还是使用`nothrow()`方法：

```java
http.async("/users/1")
        .nothrow()  // 告诉 OkHttps 发生异常时不向外抛出
        .setOnResponse((HttpResult result) -> {
            // 当发生异常时就不会走这里
        })
        .get();
```

## 取消请求

　　在 OkHttps 里取消请求共有 **4 种** 方式可选：

**1、** 使用`HttpCall#cancel()`取消单个请求（适用于异步请求，[详见`HttpCall`章节](/v2/foundation.html#httpcall)）

**2、** 使用`HttpTask#cancel()`取消单个请求（适用于所有请求）（since v1.0.4）

```java
HttpTask<?> task = http.async("/users")
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        });

task.get(); // 发起 GET 请求

// 取消请求，并返回是否取消成功
boolean canceled = task.cancel();   
```

**3、** 使用`HTTP#cancel(String tag)`按标签批量取消请求（适用于所有请求，[详见 标签 章节](/v2/foundation.html#使用标签)）

**4、** 使用`HTTP#cancelAll()`取消所有请求（适用于所有请求）（since v1.0.2）

```java
http.cancelAll();   // 取消所有请求
```

::: tip 提示
以上四种方式都对所有类型的请求有效，包括：同步 HTTP、异步 HTTP 和 WebSocket 连接。
:::

除了以上的 4 种方式，OkHttps 里还可以实现自动取消，请参考 [安卓-生命周期绑定](/v2/android.html#生命周期绑定) 章节。

<br/>

<Vssue :title="$title" />