module okhttps.gson {

    requires okhttps;
    requires data.gson;
    requires com.google.gson;

    provides com.ejlchina.okhttps.ConvertProvider with
            com.ejlchina.okhttps.gson.GsonMsgConvertor;

    exports com.ejlchina.okhttps.gson;

}