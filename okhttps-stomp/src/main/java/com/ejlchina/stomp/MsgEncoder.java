package com.ejlchina.stomp;

/**
 * Stomp 消息编码器
 */
public class MsgEncoder {

    private boolean legacyWhitespace;

    /**
     * @param input Stomp 消息
     * @return 编码后的文本
     */
    public String encode(Message input) {
        StringBuilder builder = new StringBuilder();
        builder.append(input.getCommand()).append('\n');
        for (Header header : input.getHeaders()) {
            builder.append(header.getKey()).append(':').append(header.getValue()).append('\n');
        }
        builder.append('\n');
        String payload = input.getPayload();
        if (payload != null) {
            builder.append(payload);
            if (legacyWhitespace) builder.append("\n\n");
        }
        builder.append("\u0000");
        return builder.toString();
    }

    public boolean isLegacyWhitespace() {
        return legacyWhitespace;
    }

    public void setLegacyWhitespace(boolean legacyWhitespace) {
        this.legacyWhitespace = legacyWhitespace;
    }

}
