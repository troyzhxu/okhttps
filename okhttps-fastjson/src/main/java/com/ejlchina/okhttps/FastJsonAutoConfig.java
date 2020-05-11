package com.ejlchina.okhttps;

public class FastJsonAutoConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getMsgConvertor() == null) {
            builder.msgConvertor(new FastjsonMsgConvertor());
        }
    }

}
