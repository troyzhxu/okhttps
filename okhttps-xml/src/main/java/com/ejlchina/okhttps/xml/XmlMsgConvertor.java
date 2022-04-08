package com.ejlchina.okhttps.xml;

import com.ejlchina.data.xml.XmlDataConvertor;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.MsgConvertor;

import javax.xml.parsers.DocumentBuilderFactory;

public class XmlMsgConvertor extends XmlDataConvertor implements MsgConvertor, ConvertProvider {

    public XmlMsgConvertor() { }

    public XmlMsgConvertor(DocumentBuilderFactory dbFactory) {
        super(dbFactory);
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
