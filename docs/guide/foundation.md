## 请求方法

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

## 回调函数

　　OkHttps 的回调函数全部使用单方法模式，这样可以充分利用 Java8 或 Kotlin 中的 Lambda 表达式，使你的代码更加简洁优雅：

```java
http.async("/users/{id}")             // http://api.demo.com/users/1
        .addPathParam("id", 1)
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        })
        .setOnException((IOException e) -> {
            // 异常回调
        })
        .setOnComplete((State state) -> {
            // 完成回调，无论成功失败都会执行
            // 并且在 响应|异常回调 之前执行
        })
        .get();
```

　　OkHttps 同时还支持 **全局回调** 和 **回调阻断** 机制，详见 [全局回调监听](/guide/configuration.html#全局回调监听)。

::: tip
* 只有异步请求才可以设置这三种（响应|异常|完成）回调
* 同步请求直接返回结果，无需使用回调
:::

## HttpResult

　　`HttpResult`是HTTP请求执行完后的结果，它是同步请求方法（ `get`、`post`、`put`、`delete`）的返回值，也是异步请求响应回调（`OnResponse`）的参数，它定义了如下方法：

* `getState()`         得到请求执行状态枚举，它有以下取值：
    * `State.CANCELED`      请求被取消
    * `State.RESPONSED`     已收到响应
    * `State.TIMEOUT`       请求超时
    * `State.NETWORK_ERROR` 网络错误
    * `State.EXCEPTION`     其它请求异常
* `getStatus()`        得到HTTP状态码
* `isSuccessful()`     是否响应成功，状态码在 [200..300) 之间
* `getHeaders()`       得到HTTP响应头
* `getHeaders(String name)` 得到HTTP响应头
* `getHeader(String name)`  得到HTTP响应头
* `getBody()`          得到响应报文体`Body`实例，它定义了如下方法（对同一个`Body`实例，以下的`toXXX()`类方法只能使用一个且仅能调用一次，除非先使用 cache 方法）：
    * `toBytes()`                     返回字节数组
    * `toByteStream()`                返回字节输入流
    * `toCharStream()`                返回字符输入流
    * `toString()`                    返回字符串
    * `toJsonObject()`                返回Json对象
    * `toJsonArray()`                 返回Json数组
    * `toBean(Class<T> type)`         返回根据type自动json解析后的JavaBean
    * `toList(Class<T> type)`         返回根据type自动json解析后的JavaBean列表
    * `toFile(String filePath)`       下载到指定路径
    * `toFile(File file)`             下载到指定文件
    * `toFolder(String dirPath)`      下载到指定目录
    * `toFolder(File dir)`            下载到指定目录
    * `getContentType()`              返回报文体的媒体类型
    * `getContentLength()`            返回报文体的字节长度
    * `cache()`                       缓存报文体，开启缓存后可重复使用`toXXX()`类方法
    * `close()`                       关闭报文体，未对报文体做任何消费时使用，比如只读取报文头
* `getError()`         执行中发生的异常，自动捕获执行请求时发生的 网络超时、网络错误 和 其它IO异常
* `close()`            关闭报文，未对报文体做任何消费时使用，比如只读取长度

　　示例，请求结果自动转Bean和List：

```java
// 自动转Bean
Order order = http.sync("/orders/1")
        .get().getBody().toBean(Order.class);
        
// 自动转List
List<Order> orders = http.sync("/orders")
        .get().getBody().toList(Order.class);
```

　　示例，使用 cache 方法：

```java
Body body = http.sync("/orders").get().getBody().cache();

// 使用 cache 后，可以多次使用 toXXX() 方法
System.out.println(body.toString());
System.out.println(body.toJsonArray());
System.out.println(body.toList(Order.class));
```

　　示例，获取下载文件的大小：

```java
long size = http.sync("/download/test.zip")
            .get().getBody()
            .close()             // 只是想获得文件大小，不消费报文体，所以直接关闭
            .getContentLength(); // 获得待下载文件的大小

// 由于未消费报文体，所以本次请求不会消耗下载报文体的时间和网络流量

System.out.println("size = " + size);
```

## HttpCall

　　`HttpCall`对象是异步请求方法（`get`、`post`、`put`、`delete`）的返回值，与`java`的`Future`接口很像，它有如下方法：

* `cancel()` 取消本次请求，返回取消结果
* `isCanceled()` 返回请求是否被取消
* `isDone()` 返回是否执行完成，包含取消和失败
* `getResult()` 返回执行结果`HttpResult`对象，若请求未执行完，则挂起当前线程直到执行完成再返回

　　取消一个异步请求示例：

```java
HttpCall call = http.async("/users/1").get();

System.out.println(call.isCanceled());     // false

boolean success = call.cancel();           // 取消请求

System.out.println(success);               // true
System.out.println(call.isCanceled());     // true
```

## 构建HTTP任务

　　`HTTP`对象的`sync`与`async`方法返回一个`HttpTask`对象，该对象提供了可链式调用的`addXXX`与`setXXX`等系列方法用于构建任务本身。

* `addHeader(String name, String value)`    添加请求头
* `addHeader(Map<String, String> headers)`  添加请求头

* `addPathParam(String name, Object value)` 添加路径参数：替换URL里的{name}占位符
* `addPathParam(Map<String, ?> params)`     添加路径参数：替换URL里的{name}占位符

* `addUrlParam(String name, Object value)`  添加URL参数：拼接在URL的?之后（查询参数）
* `addUrlParam(Map<String, ?> params)`      添加URL参数：拼接在URL的?之后（查询参数）

* `addBodyParam(String name, Object value)` 添加Body参数：以表单key=value&的形式放在报文体内（表单参数）
* `addBodyParam(Map<String, ?> params)`     添加Body参数：以表单key=value&的形式放在报文体内（表单参数）

* `addJsonParam(String name, Object value)` 添加Json参数：请求体为Json（支持多层结构）
* `addJsonParam(Map<String, ?> params)`     添加Json参数：请求体为Json（支持多层结构）

* `setRequestJson(Object json)`             设置请求体的Json字符串 或待转换为 Json的 JavaBean        
* `setRequestJson(Object bean, String dateFormat)` 设置请求体的Json字符串 或待转换为 Json的 JavaBean 

* `addFileParam(String name, String filePath)` 上传文件
* `addFileParam(String name, File file)` 上传文件
* `addFileParam(String name, String type, InputStream inputStream)` 上传文件
* `addFileParam(String name, String type, String fileName, InputStream input)` 上传文件
* `addFileParam(String name, String type, byte[] content)` 上传文件
* `addFileParam(String name, String type, String fileName, byte[] content)` 上传文件

* `setTag(String tag)` 为HTTP任务添加标签
* `setRange(long rangeStart)` 设置Range头信息，用于[断点续传](/guide/updown.html#实现断点续传)
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，可用于[分块下载](/guide/updown.html#实现分块下载)

* `bind(Object object)` 绑定一个对象，可用于实现Android里的[生命周期绑定](/guide/android.html#生命周期绑定)

## 使用标签

　　有时候我们想对HTTP任务加以分类，这时候可以使用标签功能：

```java
http.async("/users")    //（1）
        .setTag("A")
        .get();
        
http.async("/users")    //（2）
        .setTag("A.B")
        .get();
        
http.async("/users")    //（3）
        .setTag("B")
        .get();
        
http.async("/users")    //（4）
        .setTag("B")
        .setTag("C")    // 从 v1.0.4 标签将以追加模式添加，等效于 setTag("B.C")
        .get();
        
http.async("/users")    //（5）
        .setTag("C")
        .get();
```
　　当使用标签后，就可以按标签批量的对HTTP任务进行取消：

```java
int count = http.cancel("B");              //（2）（3）（4）被取消（取消标签包含"B"的任务）
System.out.println(count);                 // 输出 3
```
　　标签除了可以用来取消任务，在预处理器中它也可以发挥作用，请参见 [并行预处理器](/guide/configuration.html#并行预处理器) 与 [串行预处理器（token问题最佳解决方案）](/guide/configuration.html#串行预处理器（token问题最佳解决方案）)。
