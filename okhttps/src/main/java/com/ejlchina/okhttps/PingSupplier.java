package com.ejlchina.okhttps;

import okio.ByteString;

/**
 * 心跳数据提供者
 */
public interface PingSupplier {

    /**
     * @return 心跳数据
     */
    ByteString getPing();

}
