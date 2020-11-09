# 常见问题

## OkHttps 支持 HTTPS 吗？需要额外配置吗？

答：**支持，并不需要额外配置**，比如以下请求百度的网址，不需要任何配置就可以正常运行：

```java
HTTP http = HTTP.builder().build();
String baidu = http.sync("https://www.baidu.com")
    .get()
    .getBody()
    .toString();
System.out.println(baidu);
```

当然这有一个前提就是是服务器配置的 HTTPS 证书是值得信任并且有效的，这也是我们推荐的一种方式。

如果服务器的 HTTPS 证书不是在权威机构购买而是自己生成的（不推荐这种做法），则需要配置`sslSocketFactory`和`hostnameVerifier`即可：

```java
HTTP http = HTTP.builder()
    .config(b -> {
        b.sslSocketFactory(mySSLSocketFactory, myTrustManager);
        b.hostnameVerifier(myHostnameVerifier);
    })
    .build();
```

例如，让 OkHttps 信任所有:

```java
X509TrustManager myTrustManager = new X509TrustManager() {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
};

HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
};

SSLContext sslCtx = SSLContext.getInstance("TLS");
sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());

SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();

HTTP http = HTTP.builder()
    .config(b -> {
        b.sslSocketFactory(mySSLSocketFactory, myTrustManager);
        b.hostnameVerifier(myHostnameVerifier);
    })
    .build();
```
