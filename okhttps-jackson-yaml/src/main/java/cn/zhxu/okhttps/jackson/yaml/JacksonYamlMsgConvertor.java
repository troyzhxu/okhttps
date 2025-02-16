package cn.zhxu.okhttps.jackson.yaml;

import cn.zhxu.data.jackson.yaml.JacksonYamlDataConvertor;
import cn.zhxu.okhttps.ConvertProvider;
import cn.zhxu.okhttps.MsgConvertor;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class JacksonYamlMsgConvertor extends JacksonYamlDataConvertor implements MsgConvertor, ConvertProvider {

	public JacksonYamlMsgConvertor() { }
	
	public JacksonYamlMsgConvertor(YAMLMapper yamlMapper) {
		super(yamlMapper);
	}

	@Override
	public String mediaType() {
		return "application/yaml";
	}

	@Override
	public MsgConvertor getConvertor() {
		return new JacksonYamlMsgConvertor();
	}

}
