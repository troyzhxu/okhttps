package com.ejlchina.okhttps;

public class JacksonConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getJsonService() == null) {
            builder.jsonService(new JacksonService());
        }
    }

}
