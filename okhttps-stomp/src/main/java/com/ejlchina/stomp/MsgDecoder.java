package com.ejlchina.stomp;

/**
 * Stomp 消息解码器
 */
public interface MsgDecoder {

    /**
     * @param input 输入
     * @return Message
     */
    Message decode(String input);

}
