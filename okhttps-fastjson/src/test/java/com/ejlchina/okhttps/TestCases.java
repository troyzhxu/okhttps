package com.ejlchina.okhttps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


public class TestCases {

	JsonService jsonService = new FastJsonService();
	
	@Test
	public void testToJsonObj() {
		String json = "{\"id\":1,\"name\":\"Jack\"}";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		JsonObj jsonObj = jsonService.toJsonObj(in);
		Set<String> keys = jsonObj.keySet();
		Assert.assertTrue(keys.size() == 2);
		Assert.assertTrue(keys.contains("id"));
		Assert.assertTrue(keys.contains("name"));
		Assert.assertTrue(!jsonObj.isEmpty());
		Assert.assertTrue(2 == jsonObj.size());
		Assert.assertTrue(1 == jsonObj.getInt("id"));
		Assert.assertTrue(!jsonObj.has("age"));
		Assert.assertTrue(0 == jsonObj.getInt("age"));
		Assert.assertEquals("Jack", jsonObj.getString("name"));
	}
	
	@Test
	public void testToJsonArr() {
		String json = "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		JsonArr jsonObj = jsonService.toJsonArr(in);
		Assert.assertTrue(!jsonObj.isEmpty());
		Assert.assertTrue(jsonObj.size() == 2);
		JsonObj json1 = jsonObj.getJsonOjb(0);
		JsonObj json2 = jsonObj.getJsonOjb(1);
		Assert.assertEquals(1, json1.getInt("id"));
		Assert.assertEquals("Jack", json1.getString("name"));
		Assert.assertEquals(2, json2.getInt("id"));
		Assert.assertEquals("Tom", json2.getString("name"));
	}
	
	@Test
	public void testToJsonStr() {
		String json = jsonService.toJsonStr(new User(1, "Jack"));
		Assert.assertEquals("{\"id\":1,\"name\":\"Jack\"}", json);
	}
	
	@Test
	public void testToJsonStrWithDateFormat() throws ParseException {
		String dataFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
		String date = "2020-05-09 12:30:15";
		String json = jsonService.toJsonStr(new DateBean(1, sdf.parse(date)), dataFormat);
		Assert.assertTrue(json.contains(date));
	}
	
	@Test
	public void testJsonToBean() {
		String json = "{\"id\":1,\"name\":\"Jack\"}";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		User user = jsonService.jsonToBean(User.class, in);
		Assert.assertEquals(1, user.getId());
		Assert.assertEquals("Jack", user.getName());
	}
	
	@Test
	public void testJsonToList() {
		String json = "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		List<User> users = jsonService.jsonToList(User.class, in);
		Assert.assertTrue(2 == users.size());
		User u1 = users.get(0);
		User u2 = users.get(1);
		Assert.assertEquals(1, u1.getId());
		Assert.assertEquals("Jack", u1.getName());
		Assert.assertEquals(2, u2.getId());
		Assert.assertEquals("Tom", u2.getName());
	}
    
}
