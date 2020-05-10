package com.ejlchina.okhttps;

public class FastJsonAutoConfig implements OkHttps.Config {

    @Override
    public void withConfig(HTTP.Builder builder) {
        if (builder.getJsonService() == null) {
            builder.jsonService(new FastJsonService());
        }
    }

}
