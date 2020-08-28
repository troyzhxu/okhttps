package com.ejlchina.stomp;

import com.ejlchina.okhttps.Platform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {


    public static final String TERMINATE_MESSAGE_SYMBOL = "\u0000";

    private static final Pattern PATTERN_HEADER = Pattern.compile("([^:\\s]+)\\s*:\\s*([^:\\s]+)");

    private final String command;
    private final List<Header> headers;
    private final String payload;

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
        builder.append(TERMINATE_MESSAGE_SYMBOL);
        return builder.toString();
    }

    public static Message from(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new Message(Commands.UNKNOWN, null, data);
        }
        Scanner reader = new Scanner(new StringReader(data));
        reader.useDelimiter("\\n");
        String command = reader.next();
        List<Header> headers = new ArrayList<>();

        while (reader.hasNext(PATTERN_HEADER)) {
            Matcher matcher = PATTERN_HEADER.matcher(reader.next());
            if (matcher.find()) {
                headers.add(new Header(matcher.group(1), matcher.group(2)));
            }
        }
        try {
            reader.skip("\n\n");
        } catch (NoSuchElementException e) {
            Platform.logError("没有找到指定的模式", e);
        }
        reader.useDelimiter(TERMINATE_MESSAGE_SYMBOL);
        String payload = reader.hasNext() ? reader.next() : null;

        return new Message(command, headers, payload);
    }

    @Override
    public String toString() {
        return "Message {" +
                "command='" + command + '\'' +
                ", headers=" + headers +
                ", payload='" + payload + '\'' +
                '}';
    }

}
