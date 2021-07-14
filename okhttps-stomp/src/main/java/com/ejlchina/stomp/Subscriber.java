package com.ejlchina.stomp;

import com.ejlchina.okhttps.OnCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class Subscriber {

    private final Stomp stomp;
    private final String id;
    private final String destination;
    private final OnCallback<Message> callback;
    private final List<Header> headers;
    private boolean subscribed;

    public Subscriber(Stomp stomp, String destination, OnCallback<Message> callback, List<Header> headers) {
        this.stomp = stomp;
        this.id = UUID.randomUUID().toString();
        this.destination = destination;
        this.callback = callback;
        this.headers = headers;
    }

    public void subscribe() {
        if (stomp.isConnected() && !subscribed) {
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
                headers.add(new Header(Header.ACK, stomp.isAutoAck() ? Stomp.AUTO_ACK : Stomp.CLIENT_ACK));
            }
            stomp.send(new Message(Commands.SUBSCRIBE, headers, null));
            subscribed = true;
        }
    }

    public void unsubscribe() {
        List<Header> headers = Collections.singletonList(new Header(Header.ID, id));
        stomp.send(new Message(Commands.UNSUBSCRIBE, headers, null));
        resetStatus();
    }

    public void resetStatus() {
        subscribed = false;
    }

    public boolean destinationEqual(String destination) {
        return destination != null && destination.equals(this.destination);
    }

    public boolean tryCallback(String id, Message msg) {
        if (this.id.equals(id)) {
            callback.on(msg);
            return true;
        }
        return false;
    }

}
