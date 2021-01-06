package com.ejlchina.stomp;

public class Header {

    /**
     * 订阅者 ID
     */
    public static final String ID = "id";

    /**
     * 接受版本
     */
    public static final String VERSION = "accept-version";

    /**
     * 心跳
     */
    public static final String HEART_BEAT = "heart-beat";

    /**
     * 目的地
     */
    public static final String DESTINATION = "destination";

    /**
     * 内容类型
     */
    public static final String CONTENT_TYPE = "content-type";

    /**
     * 消息 ID
     */
    public static final String MESSAGE_ID = "message-id";

    /**
     * 凭据
     */
    public static final String RECEIPT = "receipt";

    /**
     * 凭据 ID
     */
    public static final String RECEIPT_ID = "receipt-id";

    /**
     * 订阅者
     */
    public static final String SUBSCRIPTION = "subscription";

    /**
     * 确认
     */
    public static final String ACK = "ack";


    private final String key;
    private final String value;


    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + ':' + value;
    }

}
