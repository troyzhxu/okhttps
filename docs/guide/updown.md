
## 文件下载

　　OkHttps 并没有把文件的下载排除在常规的请求之外，同一套API，它优雅的设计使得下载与常规请求融合的毫无违和感，一个最简单的示例：

```java
http.sync("/download/test.zip")
        .get()                           // 使用 GET 方法（其它方法也可以，看服务器支持）
        .getBody()                       // 得到报文体
        .toFile("D:/download/test.zip")  // 下载到指定的路径
        .start();                        // 启动下载

http.sync("/download/test.zip").get().getBody()                  
        .toFolder("D:/download")         // 下载到指定的目录，文件名将根据下载信息自动生成
        .start();
```
　　或使用异步连接方式：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            result.getBody().toFolder("D:/download").start();
        })
        .get();
```
　　这里要说明一下：`sync`与`async`的区别在于连接服务器并得到响应这个过程的同步与异步（这个过程的耗时在大文件下载中占比极小），而`start`方法启动的下载过程则都是异步的。

### 下载进度监听

　　就直接上代码啦，诸君一看便懂：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setStepBytes(1024)   // 设置每接收 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每接收 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 下载进度回调
            long doneBytes = process.getDoneBytes();   // 已下载字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已下载的比例
            boolean isDone = process.isDone();         // 是否下载完成
        })
        .toFolder("D:/download/")        // 指定下载的目录，文件名将根据下载信息自动生成
 //     .toFile("D:/download/test.zip")  // 指定下载的路径，若文件已存在则覆盖
        .setOnSuccess((File file) -> {   // 下载成功回调
            
        })
        .start();
```
　　值得一提的是：由于 OkHttps 并没有把下载做的很特别，这里设置的进度回调不只对下载文件起用作，即使对响应JSON的常规请求，只要设置了进度回调，它也会告诉你报文接收的进度（提前是服务器响应的报文有`Content-Length`头），例如：

```java
List<User> users = http.sync("/users")
        .get()
        .getBody()
        .setStepBytes(2)
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toList(User.class);
```

### 下载过程控制

　　过于简单：还是直接上代码：

```java
Ctrl ctrl = http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toFolder("D:/download/")
        .start();   // 该方法返回一个下载过程控制器
 
ctrl.status();      // 下载状态
ctrl.pause();       // 暂停下载
ctrl.resume();      // 恢复下载
ctrl.cancel();      // 取消下载（同时会删除文件，不可恢复）
```
　　无论是同步还是异步发起的下载请求，都可以做以上的控制：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            // 拿到下载控制器
            Ctrl ctrl = result.getBody().toFolder("D:/download/").start();
        })
        .get();
```

### 实现断点续传

　　OkHttps 对断点续传并没有再做更高层次的封装，因为这是app该去做的事情，它在设计上使各种网络问题的处理变简单的同时力求纯粹。下面的例子可以看到，OkHttps 通过一个失败回调拿到 **断点**，便将复杂的问题变得简单：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .toFolder("D:/download/")
        .setOnFailure((Failure failure) -> {         // 下载失败回调，以便接收诸如网络错误等失败信息
            IOException e = failure.getException();  // 具体的异常信息
            long doneBytes = failure.getDoneBytes(); // 已下载的字节数（断点），需要保存，用于断点续传
            File file = failure.getFile();           // 下载生成的文件，需要保存 ，用于断点续传（只保存路径也可以）
        })
        .start();
```
　　下面代码实现续传：

```java
long doneBytes = ...    // 拿到保存的断点
File file =  ...        // 待续传的文件

http.sync("/download/test.zip")
        .setRange(doneBytes)                         // 设置断点（已下载的字节数）
        .get()
        .getBody()
        .toFile(file)                                // 下载到同一个文件里
        .setAppended()                               // 开启文件追加模式
        .setOnSuccess((File file) -> {

        })
        .setOnFailure((Failure failure) -> {
        
        })
        .start();
```

### 实现分块下载

　　当文件很大时，有时候我们会考虑分块下载，与断点续传的思路是一样的，示例代码：

```java
static String url = "http://api.demo.com/download/test.zip"

public static void main(String[] args) {
    long totalSize = HttpUtils.sync(url).get().getBody()
            .close()             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
            .getContentLength(); // 获得待下载文件的大小（由于未消费报文体，所以该请求不会消耗下载报文体的时间和网络流量）
    download(totalSize, 0);      // 从第 0 块开始下载
    sleep(50000);                // 等待下载完成（不然本例的主线程就结束啦）
}

static void download(long totalSize, int index) {
    long size = 3 * 1024 * 1024;                 // 每块下载 3M  
    long start = index * size;
    long end = Math.min(start + size, totalSize);
    HttpUtils.sync(url)
            .setRange(start, end)                // 设置本次下载的范围
            .get().getBody()
            .toFile("D:/download/test.zip")      // 下载到同一个文件里
            .setAppended()                       // 开启文件追加模式
            .setOnSuccess((File file) -> {
                if (end < totalSize) {           // 若未下载完，则继续下载下一块
                    download(totalSize, index + 1); 
                } else {
                    System.out.println("下载完成");
                }
            })
            .start();
}
```

## 文件上传

　　一个简单文件上传的示例：

```java
http.sync("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()     // 上传发法一般使用 POST 或 PUT，看服务器支持
```
　　异步上传也是完全一样：

```java
http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()
```

### 上传进度监听

　　OkHttps 的上传进度监听，监听的是所有请求报文体的发送进度，示例代码：

```java
http.sync("/upload")
        .addBodyParam("name", "Jack")
        .addBodyParam("age", 20)
        .addFileParam("avatar", "D:/image/avatar.jpg")
        .setStepBytes(1024)   // 设置每发送 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 上传进度回调
            long doneBytes = process.getDoneBytes();   // 已发送字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已发送的比例
            boolean isDone = process.isDone();         // 是否发送完成
        })
        .post()
```
　　咦！怎么感觉和下载的进度回调的一样？没错！OkHttps 还是使用同一套API处理上传和下载的进度回调，区别只在于上传是在`get/post`方法之前使用这些API，下载是在`getBody`方法之后使用。很好理解：`get/post`之前是准备发送请求时段，有上传的含义，而`getBody`之后，已是报文响应的时段，当然是下载。

### 上传过程控制

　　上传文件的过程控制就很简单，和常规请求一样，只有异步发起的上传可以取消：

```java
HttpCall call = http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .post()

call.cancel();  // 取消上传
```
　　上传就没有暂停和继续这个功能啦，应该没人有这个需求吧?