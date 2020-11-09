# 常见问题

## OkHttps 支持 HTTPS 吗？需要额外配置吗？

答：支持，不需要额外配置（前提是服务器配置的 HTTPS 证书值得信任并且有效），比如以下请求百度的网址，不需要任何配置就可以正常运行：

```java
String baidu = OkHttps.sync("https://www.baidu.com")
    .get()
    .getBody()
    .toString();
System.out.println(baidu);
```
