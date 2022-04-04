package com.ejlchina.okhttps;

import java.util.ServiceLoader;

/**
 * OkHttps 配置器
 */
public interface Config {

    /**
     * 配置 {@link OkHttps } 内部的 {@link HTTP } 实例
     * @param builder HTTP.Builder
     */
    void with(HTTP.Builder builder);

    static void config(HTTP.Builder builder) {
        for (Config config : ServiceLoader.load(Config.class)) {
            config.with(builder);
        }
    }

}
