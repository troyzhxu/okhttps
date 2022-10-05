package cn.zhxu.stomp;

import java.util.List;


public class Message {

    private final String command;
    private final List<Header> headers;
    private final String payload;

    public Message(String command, List<Header> headers) {
        this(command, headers, null);
    }

    public Message(String command, List<Header> headers, String payload) {
        this.command = command;
        this.headers = headers;
        this.payload = payload;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public String getPayload() {
        return payload;
    }

    public String getCommand() {
        return command;
    }


    public String headerValue(String key) {
        Header header = header(key);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public Header header(String key) {
        if (headers != null) {
            for (Header header : headers) {
                if (header.getKey().equals(key)) return header;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Message {command='" + command + "', headers=" + headers +", payload='" + payload + "'}";
    }

}
