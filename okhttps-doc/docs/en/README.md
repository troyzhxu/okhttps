---
home: true
heroImage: /logo.png
heroText: OkHttps V2
tagline: A lightweight and powerful HTTP client on top of OkHttp
actionText: Get Started →
actionLink: /en/v2/
features:
- title: Lightweight Elegant Easy
  details: OkHttps is very lightweight, less than half the size of Retrofit, and does not depend on a specific platform. The API semantics are simple and comfortable.
- title: Out of the box features
  details: Asynchronous preprocessor, callback executor, global listener, callback prevent mechanism, file upload and download, process control, progress monitoring.
- title: More practical features
  details: URL placeholder, lambda callback, JSON auto encapsulation parsing, okhttp features like interceptor, connection pool, cookiejar, etc.

footer: Apache Licensed | Copyright © 2020-present ejlchina
---

#### <center> As Elegant as Art, As Easy as 1, 2, 3 </center>

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.example.com")
        .addMsgConvertor(new GsonMsgConvertor());
        .build();

// Synchronous HTTP
List<User> users = http.sync("/users") 
        .get()                          // GET method
        .getBody()                      // response body
        .toList(User.class);            // Automatic deserialization

// Asynchronous HTTP
http.async("/users/1")
        .setOnResponse((HttpResult res) -> {
            // Automatic deserialization
            User user = res.getBody().toBean(User.class);
        })
        .get();                         // GET method

// WebSocket
http.webSocket("/chat") 
        .onMessage((WebSocket ws，Message msg) -> {
            // Automatic deserialization
            Chat chat = msg.toBean(Chat.class);
            // Automatic Serialization
            ws.send(chat); 
        })
        .listen();                     // Start listening
```

[<center> Lean More </center>](/en/v2/)