package cn.zhxu.okhttps;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class BodyParaTests extends BaseTest {

	@Test
	public void testAddBodyPara() throws InterruptedException, UnsupportedEncodingException {
		server.enqueue(new MockResponse().setBody("OK"));

		String resp = http.sync("/")
				.addBodyPara("name", "OkHttps")
				.addBodyPara("desc", "最好用的网络框架")
				.post().getBody().toString();

		RecordedRequest request = server.takeRequest();

		Assert.assertEquals("OK", resp);
		Assert.assertTrue(request.getHeader("Content-Type").startsWith("application/x-www-form-urlencoded"));
		Assert.assertEquals("name=OkHttps&desc=最好用的网络框架", URLDecoder.decode(request.getBody().readUtf8(), "UTF-8"));
	}

	@Test
	public void testSetBodyParaFormString() throws InterruptedException {
		server.enqueue(new MockResponse().setBody("OK"));
		
		String resp = http.sync("/")
				.setBodyPara("name=OkHttps&desc=最好用的网络框架")
				.post().getBody().toString();
		
		RecordedRequest request = server.takeRequest();
		
		Assert.assertEquals("OK", resp);
		Assert.assertTrue(request.getHeader("Content-Type").startsWith("application/x-www-form-urlencoded"));
		Assert.assertEquals("name=OkHttps&desc=最好用的网络框架", request.getBody().readUtf8());
	}
	
	@Test
	public void testSetBodyParaJsonString() throws InterruptedException {
		server.enqueue(new MockResponse().setBody("OK"));
		String resp = http.sync(mockUrl)
				.bodyType("json")
				.setBodyPara("{\"name\":\"OkHttps\"}")
				.post()
				.getBody()
				.toString();
		
		RecordedRequest request = server.takeRequest();
		Assert.assertEquals("OK", resp);
		Assert.assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
		Assert.assertEquals("{\"name\":\"OkHttps\"}", request.getBody().readUtf8());
	}
	
}
