module okhttps.jackson {

    requires okhttps;
    requires data.jackson;
    requires com.fasterxml.jackson.databind;

    provides com.ejlchina.okhttps.ConvertProvider with
            com.ejlchina.okhttps.jackson.JacksonMsgConvertor;

    exports com.ejlchina.okhttps.jackson;

}