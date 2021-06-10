package com.ejlchina.stomp;

/**
 * Stomp 消息编码器
 */
public interface MsgEncoder {

    /**
     * @param input Stomp 消息
     * @return 编码后的文本
     */
    String encode(Message input);

}
