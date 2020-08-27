package com.ejlchina.stomp;

public class Header {

    public static final String VERSION = "accept-version";
    public static final String HEART_BEAT = "heart-beat";
    public static final String DESTINATION = "destination";
    public static final String CONTENT_TYPE = "content-type";
    public static final String MESSAGE_ID = "message-id";
    public static final String ID = "id";
    public static final String SUBSCRIPTION = "subscription";
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
