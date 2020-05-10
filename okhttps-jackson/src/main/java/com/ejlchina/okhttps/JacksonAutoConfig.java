package com.ejlchina.okhttps;

public class JacksonAutoConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getJsonService() == null) {
            builder.jsonService(new JacksonService());
        }
    }

}
