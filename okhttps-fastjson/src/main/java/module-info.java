module okhttps.fastjson {

    requires okhttps;
    requires data.fastjson;
    requires fastjson;
    requires okio;

    provides com.ejlchina.okhttps.ConvertProvider with
            com.ejlchina.okhttps.fastjson.FastjsonMsgConvertor;

    exports com.ejlchina.okhttps.fastjson;

}