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

	JsonService jsonService = new JacksonService();
	
	@Test
	public void testToJsonObj() {
		String json = "{\"id\":1,\"name\":\"Jack\"}";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		Mapper mapper = jsonService.toMapper(in);
		Set<String> keys = mapper.keySet();
		Assert.assertTrue(keys.size() == 2);
		Assert.assertTrue(keys.contains("id"));
		Assert.assertTrue(keys.contains("name"));
		Assert.assertTrue(!mapper.isEmpty());
		Assert.assertTrue(2 == mapper.size());
		Assert.assertTrue(1 == mapper.getInt("id"));
		Assert.assertTrue(!mapper.has("age"));
		Assert.assertTrue(0 == mapper.getInt("age"));
		Assert.assertEquals("Jack", mapper.getString("name"));
	}
	
	@Test
	public void testToJsonArr() {
		String json = "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		Array jsonObj = jsonService.toArray(in);
		Assert.assertTrue(!jsonObj.isEmpty());
		Assert.assertTrue(jsonObj.size() == 2);
		Mapper json1 = jsonObj.getMapper(0);
		Mapper json2 = jsonObj.getMapper(1);
		Assert.assertEquals(1, json1.getInt("id"));
		Assert.assertEquals("Jack", json1.getString("name"));
		Assert.assertEquals(2, json2.getInt("id"));
		Assert.assertEquals("Tom", json2.getString("name"));
	}
	
	@Test
	public void testToJsonStr() {
		String json = jsonService.serialize(new User(1, "Jack"));
		Assert.assertEquals("{\"id\":1,\"name\":\"Jack\"}", json);
	}
	
	@Test
	public void testToJsonStrWithDateFormat() throws ParseException {
		String dataFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
		String date = "2020-05-09 12:30:15";
		String json = jsonService.serialize(new DateBean(1, sdf.parse(date)), dataFormat);
		Assert.assertTrue(json.contains(date));
	}
	
	@Test
	public void testJsonToBean() {
		String json = "{\"id\":1,\"name\":\"Jack\"}";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		User user = jsonService.toBean(User.class, in);
		Assert.assertEquals(1, user.getId());
		Assert.assertEquals("Jack", user.getName());
	}
	
	@Test
	public void testJsonToList() {
		String json = "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
		InputStream in = new ByteArrayInputStream(json.getBytes());
		List<User> users = jsonService.toList(User.class, in);
		Assert.assertTrue(2 == users.size());
		User u1 = users.get(0);
		User u2 = users.get(1);
		Assert.assertEquals(1, u1.getId());
		Assert.assertEquals("Jack", u1.getName());
		Assert.assertEquals(2, u2.getId());
		Assert.assertEquals("Tom", u2.getName());
	}
    
}
