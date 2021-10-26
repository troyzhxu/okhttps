package com.ejlchina.okhttps.xml;

import com.ejlchina.data.Deserializer;
import com.ejlchina.data.xml.XmlDataConvertor;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.MsgConvertor;

import javax.xml.parsers.DocumentBuilderFactory;

public class XmlMsgConvertor extends XmlDataConvertor implements MsgConvertor, ConvertProvider {

    public XmlMsgConvertor() { }

    public XmlMsgConvertor(Deserializer deserializer, DocumentBuilderFactory dbFactory) {
        super(deserializer, dbFactory);
    }

    @Override
    public String mediaType() {
        return "application/xml; charset={charset}";
    }

    @Override
    public MsgConvertor getConvertor() {
        return new XmlMsgConvertor();
    }

}
