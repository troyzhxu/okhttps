---
description: OkHttps 安卓 生命周期 UI线程 主线程 IO线程 自由 灵活 切换 生命周期绑定 配置预处理器 定义生命周期监视器 配置全局回调监听
---

# 安卓

## 回调线程切换

　　在 Android 开发中，经常会把某些代码放到特点的线程去执行，比如网络请求响应后的页面更新在主线程（UI线程）执行，而保存文件则在IO线程操作。OkHttps 为这类问题提供了良好的方案。

　　在 **默认** 情况下，**所有回调** 函数都会 **在 IO 线程** 执行。为什么会设计如此呢？这是因为 OkHttps 只是纯粹的 Java 领域 Http工具包，本身对 Android 不会有任何依赖，因此也不知 Android 的 UI 线程为何物。这么设计也让它在 Android 之外有更多的可能性。

### 配置

　　但是在 Android 里使用  OkHttps 的话，UI线程的问题能否优雅的解决呢？当然可以！简单粗暴的方法就是配置一个 回调执行器：

 ```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            // 实际编码中可以吧 Handler 提出来，不需要每次执行回调都重新创建
            new Handler(Looper.getMainLooper()).post(run); // 在主线程执行
        })
        .build();
```

### 用例

　　上述代码便实现了让 **所有** 的 **回调函数** 都在 **主线程（UI线程）** 执行的目的，如：

```java
http.async("/users")
        .addBodyPara("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .setOnResponse((HttpResult result) -> {
            // 在主线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行
        })
        .setOnComplete((State state) -> {
            // 在主线程执行
        })
        .post();
```
　　但是，如果同时还想让某些回调放在IO线程，实现 **自由切换**，怎么办呢？OkHttps 给出了非常灵活的方法，如下：

```java
http.async("/users")
        .addBodyPara("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnResponse((HttpResult result) -> {
            // 在 IO 线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行（没有指明 nextOnIO 则在回调执行器里执行）
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnComplete((State state) -> {
            // 在 IO 线程执行
        })
        .post();
```
　　无论是哪一个回调，都可以使用`nextOnIO()`方法自由切换。同样，对于文件下载也是一样：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .toFolder("D:/download/")
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnSuccess((File file) -> {
            // 在 IO 线程执行
        })
        .setOnFailure((Failure failure) -> {
            // 在主线程执行
        })
        .start();
```

## 生命周期绑定

　　由于 OkHttps 并不依赖于 Android，所以它并没有提供关于生命周期绑定的直接实现，但它的一些扩展机制让我们很容易就可以实现这个需求。在开始之前，我们首先要理解何为生命周期绑定：

::: tip HTTP 请求的生命周期绑定
所谓的生命周期绑定：即是让 HTTP 任务感知其所属的 Activity 或 Fragment 的生命周期，当  Activity 或 Fragment 将被销毁时，框架应自动的把由它们发起的但尚未完成的 HTTP 任务全部取消，以免导致程序出错！
:::

　　现在我们需要对`HTTP`实例进行配置，配置后的`HTTP`实例具有生命周期绑定的功能，在`Activity`或`Fragment`里，它的使用效果如下：

```java
// 在 Activity 或 Fragment 内发起请求
http.async("http://www.baidu.com")
        .bind(getLifecycle())   // 绑定生命周期
        .setOnResponse((HttpResult result) -> {
            Log.i("FirstFragment", "收到请求：" + result.toString());
        })
        .get();
```
　　上述代码中的`getLifecycle()`是`Activity`或`Fragment`自带的方法，而`bind()`是`HttpTask`的现有方法。在配置好`HTTP`实例后，上述代码发起的请求便可以感知`Activity`或`Fragment`的生命周期。

　　那`HTTP`实例到底该如何配置呢？

### 第一步：配置预处理器

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .addPreprocessor((Preprocessor.PreChain chain) -> {
            HttpTask<?> task = chain.getTask();
            Object bound = task.getBound();
            // 判断 task 是否绑定了 Lifecycle 对象
            if (bound instanceof Lifecycle) {
                // 重新绑定一个 生命周期监视器（LCObserver）对象，它的定义见下一步
                task.bind(new LCObserver(task, (Lifecycle) bound));
            }
            chain.proceed();
        })
        ... // 省略其它配置项
        .build();
```

### 第二步：定义生命周期监视器

```java
public class LCObserver implements LifecycleObserver {

    HttpTask<?> task;
    Lifecycle lifecycle;

    LCObserver(HttpTask<?> task, Lifecycle lifecycle) {
        this.task = task;
        this.lifecycle = lifecycle;
        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        task.cancel();  // 在 ON_STOP 事件中，取消对应的 HTTP 任务
    }

    public void unbind() {
        // 在请求完成之后移除监视器
        lifecycle.removeObserver(this);
    }

}
```

### 第三步：配置全局回调监听

　　以上两步其实已经实现了生命周期绑定的功能，但是在请求完成之后，我们需要在`lifecycle`中移除`LCObserver`对象：

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .completeListener((HttpTask<?> task, HttpResult.State state) -> {
            Object bound = task.getBound();
            // 判断 task 是否绑定了生命周期监视器（LCObserver）对象
            if (bound instanceof LCObserver) {
                // 解绑监视器
                ((LCObserver) bound).unbind();
            }
            return true;
        })
        ... // 省略其它配置项
        .build();
```

**以上三步便在Android中实现了生命周期与HTTP请求绑定的功能**

::: tip 提示
以上三步实现的生命周期绑定，不仅对 HTTP 请求有作用，对 WebSocket 连接也同样有效哦。
:::
