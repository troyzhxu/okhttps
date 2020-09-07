import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TestCases {


    @Test
    public void testXml() {
        String xml = "<xml>\n" +
                "\t<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "\t<return_msg><![CDATA[OK]]></return_msg>\n" +
                "\t<appid><![CDATA[wxed49176a0c6f68ac]]></appid>\n" +
                "\t<mch_id><![CDATA[1509552301]]></mch_id>\n" +
                "\t<nonce_str><![CDATA[6h5FCJZcMSg5wK0r]]></nonce_str>\n" +
                "\t<sign><![CDATA[D167565B9B3D76E4ABA2C4A51D8546DF]]></sign>\n" +
                "\t<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "\t<prepay_id><![CDATA[wx0517441767554895cf13939de762940000]]></prepay_id>\n" +
                "\t<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "</xml>";

        Map<String, String> map = parseXml(new ByteArrayInputStream(xml.getBytes()));

        System.out.println("xml = " + map);

    }

    public static Map<String, String> parseXml(InputStream input) {
        // 这里用Dom的方式解析回包的最主要目的是防止API新增回包字段
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);
            // 获取到document里面的全部结点
            NodeList allNodes = document.getFirstChild().getChildNodes();
            Node node;
            Map<String, String> map = new HashMap<>();
            int i = 0;
            while (i < allNodes.getLength()) {
                node = allNodes.item(i);
                if (node instanceof Element) {
                    map.put(node.getNodeName(), node.getTextContent());
                }
                i++;
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("XML解析异常：", e);
        }
    }

}

