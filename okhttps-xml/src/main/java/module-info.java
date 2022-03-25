module okhttps.xml {

    requires okhttps;
    requires data.core;
    requires data.xml;
    requires java.xml;

    provides com.ejlchina.okhttps.ConvertProvider with
            com.ejlchina.okhttps.xml.XmlMsgConvertor;

    exports com.ejlchina.okhttps.xml;

}