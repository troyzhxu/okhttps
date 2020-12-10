package com.ejlchina.stomp;

import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.Platform;
import com.ejlchina.okhttps.WebSocket;
import com.ejlchina.okhttps.internal.WebSocketTask;

import okio.ByteString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * 基于 OkHttps websockt 的 Stomp 客户端
 */
public class Stomp {

    private static final String TOPIC = "/topic";
    private static final String QUEUE = "/queue";

    public static final String SUPPORTED_VERSIONS = "1.1,1.2";
    public static final String AUTO_ACK = "auto";
    public static final String CLIENT_ACK = "client";

    private final boolean autoAck;
    private boolean connected;
    private final WebSocketTask task;
    private WebSocket websocket;
    private boolean legacyWhitespace = false;
    private final List<Subscriber> subscribers;


    private OnCallback<Stomp> onConnected;
    private OnCallback<WebSocket.Close> onDisconnected;
    private OnCallback<Message> onError;


    private Stomp(WebSocketTask task, boolean autoAck) {
        this.task = task;
        this.autoAck = autoAck;
        this.subscribers = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * 构建 Stomp 客户端（自动确定消息）
     * @param task 底层的 WebSocket 连接
     * @return Stomp
     */
    public static Stomp over(WebSocketTask task) {
        return over(task, true);
    }

    /**
     * 构建 Stomp 客户端
     * @param task 底层的 WebSocket 连接
     * @param autoAck 是否自动确定消息
     * @return Stomp
     */
    public static Stomp over(WebSocketTask task, boolean autoAck) {
        return new Stomp(task, autoAck);
    }

    /**
     * 连接 Stomp 服务器
     * @return Stomp
     */
    public Stomp connect() {
        return connect(null);
    }

    /**
     * 连接 Stomp 服务器
     * @param headers Stomp 头信息
     * @return Stomp
     */
    public Stomp connect(List<Header> headers) {
        if (connected) {
            return this;
        }
        websocket = task.setOnOpen((ws, res) -> {
                List<Header> cHeaders = new ArrayList<>();
                cHeaders.add(new Header(Header.VERSION, SUPPORTED_VERSIONS));
                cHeaders.add(new Header(Header.HEART_BEAT,
                        task.pingSeconds() * 1000 + "," + task.pongSeconds() * 1000));
                if (headers != null) {
                    cHeaders.addAll(headers);
                }
                send(new Message(Commands.CONNECT, cHeaders, null));
            })
            .setOnMessage((ws, msg) -> {
        		Message message = Message.from(msg.toString());
        		if (message != null) {
        			receive(message);
        		}
        	})
            .setOnClosed((ws, close) -> {
                if (onDisconnected != null) {
                    onDisconnected.on(close);
                }
            })
            .listen();
        return this;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (websocket != null) {
            websocket.close(1000, "disconnect by user");
        }
    }

    /**
     * 连接成功回调
     * @param onConnected 连接成功回调
     * @return Stomp
     */
    public Stomp setOnConnected(OnCallback<Stomp> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    /**
     * 连接断开回调
     * @param onDisconnected 断开连接回调
     * @return Stomp
     */
    public Stomp setOnDisconnected(OnCallback<WebSocket.Close> onDisconnected) {
        this.onDisconnected = onDisconnected;
        return this;
    }
    
    /**
     * 错误回调（服务器返回的错误信息）
     * @param onError 错误回调
     * @return Stomp
     */
    public Stomp setOnError(OnCallback<Message> onError) {
		this.onError = onError;
		return this;
	}

    /**
     * 发送消息到主题
     * @param destination 目的地
     * @param data 消息
     */
    public void sendToTopic(String destination, String data) {
        sendTo(TOPIC + destination, data);
    }

    /**
     * 发送消息到队列
     * @param destination 目的地
     * @param data 消息
     */
    public void sendToQueue(String destination, String data) {
        sendTo(QUEUE + destination, data);
    }

	/**
     * 发送消息到指定目的地
     * @param destination 目的地
     * @param data 消息
     */
    public void sendTo(String destination, String data) {
        send(new Message(Commands.SEND, Collections.singletonList(new Header(Header.DESTINATION, destination)), data));
    }

    /**
     * 发送消息给服务器
     * @param message 消息
     */
    public void send(Message message) {
        if (websocket == null) {
            throw new IllegalArgumentException("You must call connect before send");
        }
        websocket.send(message.compile(legacyWhitespace));
    }

    /**
     * 监听主题消息
     * @param destination 监听地址
     * @param callback 消息回调
     * @return Stomp
     */
    public Stomp topic(String destination, OnCallback<Message> callback) {
        return topic(destination, null, callback);
    }

    /**
     * 监听主题消息
     * @param destination 监听地址
     * @param headers 附加头信息
     * @param callback 消息回调
     * @return Stomp
     */
    public Stomp topic(String destination, List<Header> headers, OnCallback<Message> callback) {
        return subscribe(TOPIC + destination, headers, callback);
    }

    /**
     * 监听队列消息
     * @param destination 监听地址
     * @param callback 消息回调
     * @return Stomp
     */
    public Stomp queue(String destination, OnCallback<Message> callback) {
        return queue(destination, null, callback);
    }

    /**
     * 监听队列消息
     * @param destination 监听地址
     * @param headers 附加头信息
     * @param callback 消息回调
     * @return Stomp
     */
    public Stomp queue(String destination, List<Header> headers, OnCallback<Message> callback) {
        return subscribe(QUEUE + destination, headers, callback);
    }

    /**
     * 订阅消息
     * @param destination 订阅地址
     * @param headers 附加头信息
     * @param callback 消息回调
     * @return Stomp
     */
    public synchronized Stomp subscribe(String destination, List<Header> headers, OnCallback<Message> callback) {
        if (destination == null || destination.isEmpty()) {
            throw new IllegalArgumentException("destination can not be empty!");
        }
        for (Subscriber s: subscribers) {
            if (destination.equals(s.destination)) {
                Platform.logError("Attempted subscribe to already-subscribed path!");
                return this;
            }
        }
        Subscriber subscriber = new Subscriber(destination, callback, headers);
        subscribers.add(subscriber);
        subscriber.subscribe();
        return this;
    }

    /**
     * 确认收到某条消息
     * @param message 服务器发过来的消息
     */
    public void ack(Message message) {
        Header subscription = message.header(Header.SUBSCRIPTION);
        Header msgId = message.header(Header.MESSAGE_ID);
        if (subscription != null || msgId != null) {
            List<Header> headers = new ArrayList<>();
            headers.add(subscription);
            headers.add(msgId);
            send(new Message(Commands.ACK, headers, null));
        } else {
            Platform.logError("subscription and message-id not found in " + message.toString() + ", so it can not be ack!");
        }
    }

    /**
     * 取消主题监听
     * @param destination 监听地址
     */
    public void untopic(String destination) {
        unsubscribe(TOPIC + destination);
    }

    /**
     * 取消队列监听
     * @param destination 监听地址
     */
    public void unqueue(String destination) {
        unsubscribe(QUEUE + destination);
    }

    /**
     * 取消订阅
     * @param destination 订阅地址
     */
    public synchronized void unsubscribe(String destination) {
        Iterator<Subscriber> it = subscribers.iterator();
        while (it.hasNext()) {
            Subscriber s = it.next();
            if (s.destination.equals(destination)) {
                s.unsubscribe();
                it.remove();
                break;
            }
        }
    }

    private void receive(Message msg) {
        String command = msg.getCommand();
        if (Commands.CONNECTED.equals(command)) {
            String hbHeader = msg.headerValue(Header.HEART_BEAT);
            if (hbHeader != null) {
                String[] heartbeats = hbHeader.split(",");
                int pingSeconds = Integer.parseInt(heartbeats[1]) / 1000;
                int pongSeconds = Integer.parseInt(heartbeats[0]) / 1000;
                task.heatbeat(Math.max(pingSeconds, task.pingSeconds()),
                        Math.max(pongSeconds, task.pongSeconds()));
                if (task.pingSupplier() == null) {
                	task.pingSupplier(() -> ByteString.of((byte) 0x0A));
                }
            }
            synchronized (this) {
                connected = true;
                for (Subscriber s: subscribers) {
                    s.subscribe();
                }
            }
            if (onConnected != null) {
                onConnected.on(this);
            }
        } else if (Commands.MESSAGE.equals(command)) {
            String id = msg.headerValue(Header.SUBSCRIPTION);
            if (id == null) {
                return;
            }
            for (Subscriber s: subscribers) {
                if (id.equals(s.id)) {
                    s.callback.on(msg);
                }
            }
        } else if (Commands.ERROR.equals(command)) {
        	if (onError != null) {
        		onError.on(msg);
        	}
        }
    }

    class Subscriber {

        private final String id;
        private final String destination;
        private final OnCallback<Message> callback;
        private final List<Header> headers;
        private boolean subscribed;

        Subscriber(String destination, OnCallback<Message> callback, List<Header> headers) {
            this.id = UUID.randomUUID().toString();
            this.destination = destination;
            this.callback = callback;
            this.headers = headers;
        }

        void subscribe() {
            if (connected && !subscribed) {
                List<Header> headers = new ArrayList<>();
                headers.add(new Header(Header.ID, id));
                headers.add(new Header(Header.DESTINATION, destination));
                boolean ackNotAdded = true;
                if (this.headers != null) {
                    for (Header header : this.headers) {
                        if (Header.ACK.equals(header.getKey())) {
                            ackNotAdded = false;
                        }
                        String key = header.getKey();
                        if (!Header.ID.equals(key) && !Header.DESTINATION.equals(key)) {
                            headers.add(header);
                        }
                    }
                }
                if (ackNotAdded) {
                    headers.add(new Header(Header.ACK, autoAck ? AUTO_ACK : CLIENT_ACK));
                }
                send(new Message(Commands.SUBSCRIBE, headers, null));
                subscribed = true;
            }
        }

        void unsubscribe() {
            List<Header> headers = Collections.singletonList(new Header(Header.ID, id));
            send(new Message(Commands.UNSUBSCRIBE, headers, null));
            subscribed = false;
        }

    }

    public void setLegacyWhitespace(boolean legacyWhitespace) {
        this.legacyWhitespace = legacyWhitespace;
    }

}
