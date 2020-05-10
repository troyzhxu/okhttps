package com.ejlchina.okhttps;

public class GsonAutoConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getJsonService() == null) {
            builder.jsonService(new GsonService());
        }
    }

}
