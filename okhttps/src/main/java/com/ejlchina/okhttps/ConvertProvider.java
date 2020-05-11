package com.ejlchina.okhttps;

import java.util.ServiceLoader;

/**
 * MsgConvertor 提供者
 */
public interface ConvertProvider {

    MsgConvertor getConvertor();

    static void inject(HTTP.Builder builder) {
        for (ConvertProvider provider : ServiceLoader.load(ConvertProvider.class)) {
            MsgConvertor convertor = provider.getConvertor();
            if (convertor != null) {
                builder.msgConvertor(convertor);
            }
        }
    }

}
