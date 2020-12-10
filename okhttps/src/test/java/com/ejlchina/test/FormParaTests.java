package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class FormParaTests extends BaseTest {

	@Test
	public void testSetBodyParaFormString() throws InterruptedException {
		HTTP http = HTTP.builder().build();
		
		server.enqueue(new MockResponse().setBody("OK"));
		
		String resp = http.sync(mockUrl)
			.setBodyPara("name=OkHttps&desc=最好用的网络框架")
			.post().getBody().toString();
		
		RecordedRequest request = server.takeRequest();
		
		Assert.assertEquals("OK", resp);
		Assert.assertTrue(request.getHeader("Content-Type").startsWith("application/x-www-form-urlencoded"));
		Assert.assertEquals("name=OkHttps&desc=最好用的网络框架", request.getBody().readUtf8());
	}
	
	@Test
	public void testSetBodyParaJsonString() throws InterruptedException {
		HTTP http = HTTP.builder().bodyType("json").build();
		
		server.enqueue(new MockResponse().setBody("OK"));
		
		String resp = http.sync(mockUrl)
			.setBodyPara("{\"name\":\"OkHttps\"}")
			.post().getBody().toString();
		
		RecordedRequest request = server.takeRequest();
	
		System.out.println(request.getHeader("Content-Type"));
		
		System.out.println(request.getBody().readUtf8());
		
//		Assert.assertEquals("OK", resp);
//		Assert.assertTrue(request.getHeader("Content-Type").startsWith("application/x-www-form-urlencoded"));
//		Assert.assertEquals("name=OkHttps&desc=最好用的网络框架", request.getBody().readUtf8());
	}
	
	
	
	
	class A<T> {
		
		T a;
		
		A(T a) {
			this.a = a;
		}
		
		void dosomething() {
			if (a instanceof String) {
				
			}
			if (a instanceof BigDecimal) {
				((BigDecimal) a).byteValue();
			}
			
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
