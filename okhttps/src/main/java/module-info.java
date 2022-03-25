module okhttps {

    requires okhttp3;
    requires data.core;
    requires okio;

    exports com.ejlchina.okhttps;
    exports com.ejlchina.okhttps.okhttp;

    uses com.ejlchina.okhttps.ConvertProvider;
    uses com.ejlchina.okhttps.Config;

}