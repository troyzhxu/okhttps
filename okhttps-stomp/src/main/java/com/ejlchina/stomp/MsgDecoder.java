package com.ejlchina.stomp;

import com.ejlchina.okhttps.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Stomp 消息解码器
 */
public class MsgDecoder {

    /**
     * @param input 输入
     * @return Message
     */
    public Message decode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new Message(Commands.UNKNOWN, null, input);
        }

        int cmdIndex = input.indexOf("\n");
        int mhIndex = input.indexOf("\n\n");

        if (cmdIndex >= mhIndex) {
            Platform.logError("非法的 STOMP 消息：" + input);
            return null;
        }
        String command = input.substring(0, cmdIndex);
        String[] headers = input.substring(cmdIndex + 1, mhIndex).split("\n");

        List<Header> headerList = new ArrayList<>(headers.length);
        for (String header : headers) {
            String[] hv = header.split(":");
            if (hv.length == 2) {
                headerList.add(new Header(hv[0], hv[1]));
            }
        }
        String payload = null;
        if (input.length() > mhIndex + 2) {
            if (input.endsWith("\u0000\n") && input.length() > mhIndex + 4) {
                payload = input.substring(mhIndex + 2, input.length() - 2);
            } else if (input.endsWith("\u0000") && input.length() > mhIndex + 3) {
                payload = input.substring(mhIndex + 2, input.length() - 1);
            }
        }
        return new Message(command, headerList, payload);
    }

}
