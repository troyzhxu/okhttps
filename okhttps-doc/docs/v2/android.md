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

### 总结

上述实现生命周期绑定的过程，关键在于对`HttpTask`的`bind(Object object)`方法传入一个`LifeCycle`对象，然后在预处理器和全局监听里做了相关处理。实际上，我们还可以在调用`bind`方法时传入一个`Activity`或`Fragment`对象，这样在预处理器和全局监听里，就不仅可以得到`LifeCycle`，还可以得到`Context`对象，可参考下文的 [安卓-最佳实践](/v2/android.html#最佳实践)。

::: tip 提示
以上三步实现的生命周期绑定，不仅对 HTTP 请求有作用，对 WebSocket 连接也同样有效哦。
:::


## 自动加载框

在调用一个耗时较长的接口时，我们往往需要显示一个加载框，以便让用户知道我们的 APP 正在工作而不是卡死。

一般的做法，是这样实现：

```java
showLoading();  // 在请求开始之前显示加载框

http.async('/api/...')
        ...
        .setOnComplete(state -> {
            // 在请求结束（成功|失败|异常）之后关闭加载框
            hideLoading();
        })
        .post();
```

这样当然可以解决问题，但是一个应用开发下来至少涉及到有三四十个接口请求，多的甚至到成百上千个，如果每个加载框的逻辑都这么写，会造成很多冗余代码，很不优雅。

那可不可以，在请求接口时，我们只给一个标签，如果有这个标签，请求时就自动显示加载框，结束后就自动关闭加载框呢？像这样：

```java
http.async('/api/...')
        .tag("LOADING")     // 自动显示和关闭加载框
        ...
        .post();
```

当然可以！OkHttps 扩展机制的强大，一不小心又体现了。来，我们直接看实现代码！

### 第一步：配置预处理器

预处理器是在网络请求开始之前执行，我们可以再这里显示加载框：

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .addPreprocessor(chain -> {
            HttpTask<?> task = chain.getTask();
            // 根据标签判断是否显示加载框
            if (task.isTagged("LOADING")) {
                showLoading(context(task)); // 显示加载框
            }
            chain.proceed();
        })
        ... // 省略其它配置项
        .build();
```

其中第9行使用了一个`context(HttpTask<?> task)`方法，它的作用是从`HttpTask`的绑定对象中取得一个`Context`对象，用于创建加载框。

### 第二步：配置全局监听

```java
HTTP http = HTTP.builder()
        ... // 省略其它配置项
        .completeListener((HttpTask<?> task, HttpResult.State state) -> {
            if (task.isTagged("LOADING")) {
                hideLoading();              // 关闭加载框
            }
            return true;
        });
        ... // 省略其它配置项
        .build();
```

### 总结

以上两步变实现了网络请求时的自动显示与关闭加载框的功能，是不是很简单呢，全部代码可参考下文的 [安卓-最佳实践](/v2/android.html#最佳实践)。

## 最佳实践

本章节，将以源代码的方式为你呈现使用 OkHttps 的正确姿势，包括：

* 默认回调线程
* 生命周期绑定
* 加载框自动显示关闭
* TOKEN 自动添加与刷新
* 错误码统一处理与回调阻断

### 标签常量

由于或多或少需要用到一些标签，我们最好把它定义成常量：

```java
public class Tags {

    /**
     * 用于标记某接口需要 Token 头信息
     * 如果没有办法得到合法的 Token，则跳转登录
     */
    public static final String TOKEN = "TOKEN";

    /**
     * 用于标记某接口是否开启自动加载框功能
     */
    public static final String LOADING = "LOADING";

    // 若有其它的想法，还可以定义其它的标签
    // ...
}
```

### 接口路径常量

由于同一个接口可能会在多处使用，所以我们可以把它们定义为常量（这一步可选）：

```java
public class Urls {

    /**
     * BaseUrl 还可以根据 build.gradle 的配置来取
     * 打出不同环境的包，自动使用不同的 BaseUrl，这里便不再示例
     */
    public static final String BASE_URL = "https://api.example.com";

    /**
     * 当 Token 快过期时，调用该接口来刷新 Token
     */
    public static final String TOKEN_REFRESH = "/oauth/access-token";

    // 其它接口 ...
}
```

### 配置 OkHttps

由于我们推荐在主应用中直接使用`OkHttps`类，所以我们在`OkHttpsConfig`中配置，可参考 [起步-配置 OkHttps](/v2/getstart.html#配置okhttps) 章节。

#### 主干配置

```java
public class OkHttpsConfig implements Config {

    // 绑定到主线程的 Handler
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void with(HTTP.Builder builder) {

        builder.baseUrl(Urls.BASE_URL)          // 配置 BaseURL

                // 如果默认请求体是JSON，则开启，否则默认为表单
                //.bodyType("json")

                // 配置默认回调在主线程执行
                .callbackExecutor(run -> mainHandler.post(run))

                // 加载框预处理（在 TOKEN 处理之前）
                .addPreprocessor(chain -> {
                    HttpTask<?> task = chain.getTask();
                    // 根据标签判断是否显示加载框
                    if (task.isTagged(Tags.LOADING)) {
                        showLoading(context(task)); 
                    }
                    chain.proceed();
                })

                // 实现生命周期绑定
                .addPreprocessor(chain -> {
                    HttpTask<?> task = chain.getTask();
                    Object bound = task.getBound();
                    task.bind(new BoundWrapper(task, bound));
                    chain.proceed();
                })

                // TOKEN 处理（串行预处理）
                .addSerialPreprocessor(chain -> {
                    HttpTask<?> task = chain.getTask();
                    // 根据标签判断是否需要 Token
                    if (!task.isTagged(Tags.TOKEN)) {
                        chain.proceed();
                        return;
                    }
                    Context ctx = context(task);
                    requestTokenAndRefreshIfExpired(ctx, chain.getHttp(),
                            (String token) -> {
                        if (token != null) {
                            // 添加 Token 头信息，名字需要和后端商定
                            task.addHeader("Access-Token", token);
                        } else {
                            // 若没有得到 Token, 则跳转登录页面
                            ctx.startActivity(new Intent(ctx, LoginActivity.class));
                        }
                        // 无论如何，这行代码一定要执行到，不然后续接口会一直在排队中
                        chain.proceed();
                    });
                })

                // 错误码统一处理
                .responseListener((HttpTask<?> task, HttpResult result) -> {
                    // 刷新 Token 的接口可以例外
                    if (task.getUrl().contains(Urls.TOKEN_REFRESH) 
                            || result.isSuccessful()) {
                        return true;            // 继续接口的业务处理
                    }
                    // 向用户展示接口的错误信息
                    showMsgToUser(task, result.getBody().toString());
                    return false;               // 阻断
                })

                // 生命周期绑定：第三步
                .completeListener((HttpTask<?> task, HttpResult.State state) -> {
                    Object bound = task.getBound();
                    if (bound instanceof BoundWrapper) {
                        ((BoundWrapper) bound).unbind();
                    }
                    // 网络错误统一处理
                    switch (state) {
                        case TIMEOUT:
                            showMsgToUser(task, "网络连接超时");
                            break;
                        case NETWORK_ERROR:
                            showMsgToUser(task, "网络错误，请检查WIFI或数据是否开启");
                            break;
                        case EXCEPTION:
                            showMsgToUser(task, "接口请求异常: " + task.getUrl());
                            break;
                    }
                    if (task.isTagged(Tags.LOADING)) {
                        hideLoading();          // 关闭加载框
                    }
                    return true;
                });
    }

    // 其它 ...
}
```

上述是一个主干配置，其中涉及到`showLoading`方法、`hideLoading`方法、`BoundWrapper`类、`context`方法、`showMsgToUser`方法 和 `requestTokenAndRefreshIfExpired`方法，下面依次给出它们的实现：

#### `showLoading`和`hideLoading`方法

``` java
public class OkHttpsConfig implements Config {

    // 省略其它...

    private ProgressDialog loading = null;
    private AtomicInteger loadings = new AtomicInteger(0);

    // 显示加载框
    private void showLoading(Context ctx) {
        if (loading == null) {
            // 这里就用 ProgressDialog 来演示了，当然可以替换成你喜爱的加载框
            loading = new ProgressDialog(ctx);
            loading.setMessage("正在加载，请稍等...");
        }
        // 增加加载框显示计数
        loadings.incrementAndGet();
        loading.show();
    }

    // 关闭加载框
    private void hideLoading() {
        // 判断是否所有显示加载框的接口都已完成
        if (loadings.decrementAndGet() <= 0
                && loading != null) {
            loading.dismiss();
            loading = null;
        }
    }

    // 其它 ...
}
```

#### `BoundWrapper`类

它可以单独做为一个类，也可以写成内部类：

```java
public class OkHttpsConfig implements Config {

    // 省略其它...

    static class BoundWrapper implements LifecycleObserver {

        HttpTask<?> task;
        Lifecycle lifecycle;
        Object bound;

        BoundWrapper(HttpTask<?> task, Object bound) {
            this.task = task;
            if (bound instanceof LifecycleOwner) {
                lifecycle = ((LifecycleOwner) bound).getLifecycle();
                lifecycle.addObserver(this);
            }
            this.bound = bound;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop() {
            task.cancel();  // 在 ON_STOP 事件中，取消对应的 HTTP 任务
        }

        void unbind() {
            if (lifecycle != null) {
                lifecycle.removeObserver(this);
            }
        }

    }

    // 其它 ...
}
```

#### `context`方法

```java
public class OkHttpsConfig implements Config {

    // 省略其它...

    /**
     * 获取 Context 对象
     **/
    private Context context(HttpTask<?> task) {
        Object bound = task.getBound();
        if (bound instanceof BoundWrapper) {
            bound = ((BoundWrapper) bound).bound;
        }
        if (bound instanceof Context) {
            return (Context) bound;
        }
        if (bound instanceof Fragment) {
            return ((Fragment) bound).getActivity();
        }
        // 还可以添加更多获取 Context 的策略，比如从全局 Application 里取
        return null;
    }

    // 其它 ...
}
```

#### `showMsgToUser`方法

```java
public class OkHttpsConfig implements Config {

    // 省略其它...

    /**
     * 向用户展示信息
     **/
    private void showMsgToUser(HttpTask<?> task, String message) {
        Context ctx = context(task);
        if (ctx != null) {
            // 这里就简单用 Toast 示例一下，有更高级的实现可以替换
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }
    }

    // 其它 ...
}
```

#### `requestTokenAndRefreshIfExpired`方法

```java
public class OkHttpsConfig implements Config {

    // 省略其它...

    /**
     * 获取TOKEN，若过期则刷新（代码中的字符串可以替换为常量）
     **/
    private void requestTokenAndRefreshIfExpired(Context ctx, HTTP http, 
            OnCallback<String> callback) {
        if (ctx == null) {
            callback.on(null);
            return;
        }
        // 这里演示使用 Preference 存储，也可以使用数据库存储
        SharedPreferences token = ctx.getSharedPreferences("token", 
                Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        // 刷新令牌
        long refreshToken = token.getString("refreshToken", null);
        // 判断有效期可以提前 60 秒，以防在接下来的网络延迟中过期了
        if (token.getLong("refreshTokenExpiresAt", 0) < now + 60000 
                || refreshToken == null) {
            // 刷新令牌已过期，说明长时间未使用，需要重新登录
            callback.on(null);
            return;
        }
        // 访问令牌
        String accessToken = token.getString("accessToken", null);
        if (token.getLong("accessTokenExpiresAt", 0) > now + 60000 
                && accessToken != null) {
            // 访问令牌未过期，则回调令牌
            callback.on(accessToken);
            return;
        }
        // 访问令牌已过期，刷新令牌未过期，则调接口刷新当前令牌
        http.async(Urls.TOKEN_REFRESH)
                .addBodyPara("refreshToken", refreshToken)
                .skipSerialPreproc()    // 跳过串行预处理器
                .setOnResponse(res -> {
                    if (!res.isSuccessful()) {
                        callback.on(null);
                        return;
                    }
                    Mapper mapper = res.getBody().toMapper();
                    String newRefreshToken = mapper.getString("refreshToken");
                    String newAccessToken = mapper.getString("accessToken");
                    int refreshTokenExpiresIn = mapper.getString("refreshTokenExpiresIn");
                    int accessTokenExpiresIn = mapper.getString("accessTokenExpiresIn");
                    // 因为发生了请求，当前时间已经变化，所有重新获取时间
                    long now2 = System.currentTimeMillis();
                    // 保存到 SharedPreferences
                    token.edit()
                            .putString("refreshToken", newRefreshToken)
                            .putString("accessToken", newAccessToken)
                            .putLong("refreshTokenExpiresAt", refreshTokenExpiresIn * 1000 + now2)
                            .putLong("accessTokenExpiresAt", accessTokenExpiresIn * 1000 + now2)
                            .commit();
                    // 回调令牌
                    callback.on(newAccessToken);
                })
                .setOnException(e -> callback.on(null))
                .post();
    }

}
```

### 使用效果

#### 在 Activity 或 Fragment 中使用

```java
OkHttps.async(Urls.SOME_URL)
        .bind(this)             // 绑定（生命周期|Context获取）
        .tag(Tags.TOKEN)        // 自动添加 TOEKN
        .tag(Tags.LOADING)      // 自动显示加载框
        // 请求参数设置...
        .setOnResponse(res -> {
            // TODO: 正确响应处理
        })
        .post();
```

::: tip
上述中的`this`得指向 Activity 或 Fragment 本身，如果是在按钮的点击事件中请求，需要指明为`YourActivity.this`或者`YourFragment.this`
:::

#### 在其它能获得 Context 对象的环境里使用

```java
OkHttps.async(Urls.SOME_URL)
        .bind(context)          // 绑定（生命周期|Context获取）
        .tag(Tags.TOKEN)        // 自动添加 TOEKN
        .tag(Tags.LOADING)      // 自动显示加载框
        // 请求参数设置...
        .setOnResponse(res -> {
            // TODO: 正确响应处理
        })
        .post();
```

### 类 Retrofit 方式

如果你是 Retrofit 用户，喜欢像 Retrofit 那样把网络接口分类归到不同的Java文件中，完全没有问题，比如你可以这样：

#### 定义 UserService

```java
public class UserService {

    private Context context;

    public UserService(Context context) {
        this.context = context;
    }

    /**
     * 获取我的信息
     **/
    public AsyncHttpTask updatePasswrod(String passwrod) {
        return OkHttps.async("/mine/passwrod")
                .bind(context) 
                .tag(Tags.TOKEN)        // 自动添加 TOEKN
                .tag(Tags.LOADING)      // 自动显示加载框
                .addBodyPara("passwrod", passwrod);
    }

    // 其它接口...
}
```

#### 使用 UserService

```java
UserService userService = new UserService(this)

String newPassword = "123456";

userService.updatePasswrod(newPassword)
        .setOnResponse(res -> {
            // 密码更新成功
        })
        .put();

```

<br/>

<Vssue :title="$title" />