package cn.zhxu.okhttps.jackson.xml;

import cn.zhxu.data.jackson.xml.JacksonXmlDataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class JacksonXmlMsgConvertor extends JacksonXmlDataConvertor implements MsgConvertor, ConvertProvider {

	public JacksonXmlMsgConvertor() { }
	
	public JacksonXmlMsgConvertor(XmlMapper xmlMapper) {
		super(xmlMapper);
	}

	@Override
	public String mediaType() {
		return "application/xml";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new JacksonXmlMsgConvertor();
	}

}
