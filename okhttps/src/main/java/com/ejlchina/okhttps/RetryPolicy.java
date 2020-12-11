package com.ejlchina.okhttps;


import java.io.IOException;

/**
 * @since 2.5.0
 * 重试策略
 */
public interface RetryPolicy {

    /**
     *执行策略
     * @param condition 当前的重试条件
     * @param tryOpt 重试选择器（要么重试、要么放弃）
     */
    void doPolicy(Condition condition, TryOpt tryOpt);

    /**
     * 重试条件
     */
    interface Condition {

        /**
         * @return 已重试次数
         */
        int getRetryTimes();

        /**
         * @return 当前的请求任务
         */
        HttpTask<?> getTask();

        /**
         * @return 当前的 Http 结果
         */
        HttpResult getHttpResult();

        /**
         * @return WebSocket 关闭原因
         */
        WebSocket.Close getClose();

        /**
         * @return 发生的 IO 异常
         */
        IOException getIOException();

    }

    /**
     * 重试选择
     */
    interface TryOpt {

        /**
         * 重试
         */
        void tryIt();

        /**
         * 放弃
         */
        void giveUp();

    }

}
