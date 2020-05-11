package com.ejlchina.okhttps;

import java.util.ServiceLoader;

/**
 * OkHttps 配置器
 */
public interface Config {

    void with(HTTP.Builder builder);

    static void config(HTTP.Builder builder) {
        for (Config config : ServiceLoader.load(Config.class)) {
            config.with(builder);
        }
    }

}
