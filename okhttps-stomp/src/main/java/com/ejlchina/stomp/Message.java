package com.ejlchina.stomp;

import java.util.ArrayList;
import java.util.List;

import com.ejlchina.okhttps.Platform;


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


    public String compile(boolean legacyWhitespace) {
        StringBuilder builder = new StringBuilder();
        builder.append(command).append('\n');
        for (Header header : headers) {
            builder.append(header.getKey()).append(':').append(header.getValue()).append('\n');
        }
        builder.append('\n');
        if (payload != null) {
            builder.append(payload);
            if (legacyWhitespace) builder.append("\n\n");
        }
        builder.append("\u0000");
        return builder.toString();
    }

    public static Message from(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new Message(Commands.UNKNOWN, null, data);
        }
        
        int cmdIndex = data.indexOf("\n");
        int mhIndex = data.indexOf("\n\n");
        
        if (cmdIndex >= mhIndex) {
        	Platform.logError("非法的 STOMP 消息：" + data);
        	return null;
        }
        String command = data.substring(0, cmdIndex);
        String[] headers = data.substring(cmdIndex + 1, mhIndex).split("\n");
        
        List<Header> headerList = new ArrayList<>(headers.length);
        for (String header : headers) {
        	String[] hv = header.split(":");
        	if (hv.length == 2) {
        		headerList.add(new Header(hv[0], hv[1]));
        	}
        }
        String payload = null;
        if (data.length() > mhIndex + 2) {
        	if (data.endsWith("\u0000\n") && data.length() > mhIndex + 4) {
        		payload = data.substring(mhIndex + 2, data.length() - 2);
        	} else if (data.endsWith("\u0000") && data.length() > mhIndex + 3) {
        		payload = data.substring(mhIndex + 2, data.length() - 1);
        	}
        }
        return new Message(command, headerList, payload);
    }

    @Override
    public String toString() {
        return "Message {command='" + command + "', headers=" + headers +", payload='" + payload + "'}";
    }

}
