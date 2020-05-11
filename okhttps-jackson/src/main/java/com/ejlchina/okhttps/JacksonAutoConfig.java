package com.ejlchina.okhttps;

public class JacksonAutoConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getMsgConvertor() == null) {
            builder.msgConvertor(new JacksonMsgConvertor());
        }
    }

}
