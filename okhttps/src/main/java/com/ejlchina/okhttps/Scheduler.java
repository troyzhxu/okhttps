package com.ejlchina.okhttps;

import java.util.concurrent.TimeUnit;

/**
 * 调度器
 */
public interface Scheduler {

    /**
     * 延时执行任务
     * @param task 任务
     * @param delay 延迟时间
     * @param unit 时间单位
     */
    void schedule(Runnable task, int delay, TimeUnit unit);

}
