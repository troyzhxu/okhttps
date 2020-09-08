package com.ejlchina.okhttps.xml;

import com.ejlchina.okhttps.Array;
import com.ejlchina.okhttps.ConvertProvider;
import com.ejlchina.okhttps.Mapper;
import com.ejlchina.okhttps.MsgConvertor;
import com.ejlchina.okhttps.internal.HttpException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

public class XmlMsgConvertor implements MsgConvertor, ConvertProvider {

    private String[] nameKeys = {"name", "key"};

    private DocumentBuilderFactory dbFactory;

    public XmlMsgConvertor() {
        this(DocumentBuilderFactory.newInstance());
    }

    public XmlMsgConvertor(DocumentBuilderFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    @Override
    public String mediaType() {
        return "application/xml";
    }

    private Element parseElement(InputStream in, Charset charset) {
        DocumentBuilder builder;
        try {
            // DocumentBuilder 是线程不安全的，所有每次解析都得新起一个 Builder
            builder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new HttpException("DocumentBuilderFactory 配置异常", e);
        }
        try {
            InputSource source = new InputSource(in);
            source.setEncoding(charset.name());
            return builder.parse(source).getDocumentElement();
        } catch (SAXException|IOException e) {
            throw new HttpException("XML 解析异常", e);
        }
    }

    @Override
    public Mapper toMapper(InputStream in, Charset charset) {
        Element root = parseElement(in, charset);
        return new XmlMapper(nameKeys, root);
    }

    @Override
    public Array toArray(InputStream in, Charset charset) {
        Element root = parseElement(in, charset);
        return new XmlArray(nameKeys, root.getChildNodes());
    }

    @Override
    public byte[] serialize(Object object, Charset charset) {
        return new byte[0];
    }

    @Override
    public <T> T toBean(Type type, InputStream in, Charset charset) {
        return null;
    }

    @Override
    public <T> List<T> toList(Class<T> type, InputStream in, Charset charset) {
        return null;
    }

    @Override
    public MsgConvertor getConvertor() {
        return new XmlMsgConvertor();
    }

    public String[] getNameKeys() {
        return nameKeys;
    }

    public void setNameKeys(String[] nameKeys) {
        this.nameKeys = nameKeys;
    }

    public DocumentBuilderFactory getDbFactory() {
        return dbFactory;
    }

    public void setDbFactory(DocumentBuilderFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

}
