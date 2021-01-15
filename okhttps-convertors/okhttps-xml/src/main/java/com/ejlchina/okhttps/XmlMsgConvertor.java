package com.ejlchina.okhttps;

import javax.xml.parsers.DocumentBuilderFactory;

public class XmlMsgConvertor extends XmlDataConvertor implements MsgConvertor, ConvertProvider {

    public XmlMsgConvertor() {
        super();
    }

    public XmlMsgConvertor(Deserializer deserializer, DocumentBuilderFactory dbFactory) {
        super(deserializer, dbFactory);
    }

    @Override
    public String mediaType() {
        return "application/xml";
    }

    @Override
    public MsgConvertor getConvertor() {
        return new XmlMsgConvertor();
    }

}
