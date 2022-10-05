package cn.zhxu.okhttps.jackson;

import cn.zhxu.data.jackson.JacksonDataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonMsgConvertor extends JacksonDataConvertor implements MsgConvertor, ConvertProvider {

	public JacksonMsgConvertor() { }
	
	public JacksonMsgConvertor(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public String mediaType() {
		return "application/json";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new JacksonMsgConvertor();
	}

}
