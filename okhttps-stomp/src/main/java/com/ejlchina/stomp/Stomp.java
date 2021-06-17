package com.ejlchina.stomp;

import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.Platform;
import com.ejlchina.okhttps.WebSocket;
import com.ejlchina.okhttps.internal.WebSocketTask;

import okio.ByteString;

import java.util.*;

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
    private boolean connected = false;      // 是否已连接
    private boolean connecting = false;     // 是否连接中
    private boolean disconnecting = false;  // 是否断开连接中
    private final WebSocketTask task;
    private WebSocket websocket;

    private final List<Subscriber> subscribers;

    private OnCallback<Stomp> onConnected;
    private OnCallback<WebSocket.Close> onDisconnected;
    private OnCallback<Throwable> onException;
    private OnCallback<Message> onError;

    private final String disReceipt;

    private MsgCodec msgCodec = new MsgCodecImpl();


    private Stomp(WebSocketTask task, boolean autoAck) {
        this.task = task;
        this.autoAck = autoAck;
        this.subscribers = Collections.synchronizedList(new ArrayList<>());
        this.disReceipt = UUID.randomUUID().toString();
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
     * @since 2.5.0
     * @return 是否自动确认消息
     */
    public boolean isAutoAck() {
        return autoAck;
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
    public synchronized Stomp connect(List<Header> headers) {
        if (connected || connecting) {
            return this;
        }
        websocket = task
            .setOnOpen((ws, res) -> doOnOpened(headers))
            .setOnMessage((ws, msg) -> msgCodec.decode(msg.toString(), this::receive))
            .setOnException((ws, e) -> doOnException(e))
            .setOnClosed((ws, close) -> doOnClosed(close))
            .listen();
        connecting = true;
        return this;
    }

    private void doOnOpened(List<Header> headers) {
        int pingSecs = task.pingSeconds();
        int pongSecs = task.pongSeconds();
        List<Header> cHeaders = new ArrayList<>();
        cHeaders.add(new Header(Header.VERSION, SUPPORTED_VERSIONS));
        if (pingSecs > 0 && pongSecs > 0) {
            cHeaders.add(new Header(Header.HEART_BEAT, pingSecs * 1000 + "," + pongSecs * 1000));
        }
        if (headers != null) {
            cHeaders.addAll(headers);
        }
        send(new Message(Commands.CONNECT, cHeaders, null));
    }

    private void doOnException(Throwable throwable) {
        OnCallback<Throwable> listener = onException;
        if (listener != null) {
            listener.on(throwable);
        }
        connecting = false;
    }

    private void doOnClosed(WebSocket.Close close) {
        connected = false;
        connecting = false;
        disconnecting = false;
        websocket = null;
        for (Subscriber subscriber : subscribers) {
            subscriber.resetStatus();
        }
        if (onDisconnected != null) {
            onDisconnected.on(close);
        }
    }

    /**
     * @since 2.5.0
     * @return 是否已连接
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @since 3.1.0
     * @return 是否正在连接
     */
    public boolean isConnecting() {
        return connecting;
    }

    /**
     * @since 3.1.0
     * @return 是否正在断开连接
     */
    public boolean isDisconnecting() {
        return disconnecting;
    }

    /**
     * 断开连接，将先发送 DISCONNECT 消息给服务器，服务器回复后断开连接
     * 默认等待服务器为 10 秒，10秒后自动关闭
     */
    public void disconnect() {
        disconnect(10);
    }

    /**
     * @since v3.1.0
     * 断开连接，将先发送 DISCONNECT 消息给服务器，服务器回复后断开连接
     * @param maxWaitSeconds 最大等待服务器回复时间，超出后自动关闭
     */
    public void disconnect(int maxWaitSeconds) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                disconnect(true);
            }
        }, 1000L * maxWaitSeconds);
        Header header = new Header(Header.RECEIPT, disReceipt);
        List<Header> headers = Collections.singletonList(header);
        send(new Message(Commands.DISCONNECT, headers));
        disconnecting = true;
    }

    /**
     * 断开连接
     * @param immediate 是否立即断开
     * @since v3.1.0
     */
    public void disconnect(boolean immediate) {
        if (immediate) {
            WebSocket ws = websocket;
            if (ws != null) {
                ws.close(1000, "disconnect by user");
                websocket = null;
            }
        } else {
            disconnect(10);
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
     * 错误回调（底层连接异常）
     * @since v3.1.1
     * @param onException 异常回调
     * @return Stomp
     */
    public Stomp setOnException(OnCallback<Throwable> onException) {
        this.onException = onException;
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
     * @since 2.5.0
     * 发送消息到主题
     * @param destination 目的地
     * @param data 消息
     */
    public void sendToTopic(String destination, String data) {
        sendTo(TOPIC + destination, data);
    }

    /**
     * @since 2.5.0
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
        WebSocket ws = websocket;
        if (ws == null) {
            throw new IllegalArgumentException("You must call connect before send");
        }
        ws.send(msgCodec.encode(message));
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
            if (s.destinationEqual(destination)) {
                throw new IllegalStateException("The destination [" + destination + "] has already been subscribed!");
            }
        }
        Subscriber subscriber = new Subscriber(this, destination, callback, headers);
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
            if (s.destinationEqual(destination)) {
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
            onConnectedFrameReceived(hbHeader);
        } else if (Commands.MESSAGE.equals(command)) {
            String id = msg.headerValue(Header.SUBSCRIPTION);
            if (id != null) {
                for (Subscriber s: subscribers) {
                    if (s.tryCallback(id, msg)) {
                        break;
                    }
                }
            }
        } else if (Commands.RECEIPT.equals(command)) {
            if (disReceipt.equals(msg.headerValue(Header.RECEIPT_ID))) {
                // 断开连接
                disconnect(true);
            }
        } else if (Commands.ERROR.equals(command)) {
        	if (onError != null) {
        		onError.on(msg);
        	}
        }
    }

    private void onConnectedFrameReceived(String hbHeader) {
        int pingSecs = task.pingSeconds();
        int pongSecs = task.pongSeconds();
        if (hbHeader != null && (pingSecs > 0 || pongSecs > 0)) {
            String[] heartbeats = hbHeader.split(",");
            int pingSeconds = Integer.parseInt(heartbeats[1]) / 1000;
            int pongSeconds = Integer.parseInt(heartbeats[0]) / 1000;
            if (pingSeconds > 0 || pongSeconds > 0) {
                if (task.pingSupplier() == null) {
                    task.pingSupplier(() -> ByteString.of((byte) 0x0A));
                }
                task.heatbeat(Math.max(pingSeconds, pingSecs), Math.max(pongSeconds, pongSecs));
            }
        }
        synchronized (this) {
            for (Subscriber s: subscribers) {
                s.subscribe();
            }
            connected = true;
            connecting = false;
        }
        if (onConnected != null) {
            onConnected.on(this);
        }
    }

    public MsgCodec getMsgCodec() {
        return msgCodec;
    }

    public void setMsgCodec(MsgCodec msgCodec) {
        this.msgCodec = msgCodec;
    }

}
