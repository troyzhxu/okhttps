package com.ejlchina.okhttps.test;

import com.ejlchina.okhttps.MsgConvertor;


public class XmlTestCases extends BaseTestCases {

	public XmlTestCases(MsgConvertor msgConvertor) {
		super(msgConvertor);
	}

	@Override
	String getUser1Str() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><user><id>1</id><name>Jack</name></user>";
	}

	@Override
	String getUser1ResultStr() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><result>\n" +
				"\t<code>200</code>\n" +
				"\t<msg>ok</msg>\n" +
				"\t<data>\n" +
				"\t\t<id>1</id><name>Jack</name>\n" +
				"\t</data>\n" +
				"</result>";
	}

	@Override
	String getUserListStr() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><list><user><id>1</id><name>Jack</name></user><user><id>2</id><name>Tom</name></user></list>";
	}
    
}
