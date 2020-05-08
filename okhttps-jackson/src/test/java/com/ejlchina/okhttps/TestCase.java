package com.ejlchina.okhttps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.ejlchina.okhttps.internal.HttpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

public class TestCase {

	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Test
	public void test() throws JsonProcessingException {
		
		User u1 = new User(1, "Tom");

		ObjectMapper mapper = objectMapper.copy();
		
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		
		String json = mapper.writeValueAsString(u1);
		
		System.out.println("json = " + json);
		
		json = objectMapper.writeValueAsString(u1);
		
		System.out.println("json = " + json);
	}
	
	
	@Test
	public void test1() throws JsonProcessingException {
		
		User u1 = new User(1, "Tom");
		User u2 = new User(2, "Jack");
		List<User> list = new ArrayList<>();
		list.add(u1);
		list.add(u2);
		
		String json = objectMapper.writeValueAsString(list);
		
		System.out.println("json = " + json);
		
		List<User> nlist = jsonToList(User.class, json);
		
		System.out.println(nlist);
	}
	
	

	public <T> List<T> jsonToList(Class<T> type, String json) {
		if (json != null) {
			CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
			try {
				return objectMapper.readValue(json, javaType);
			} catch (JsonProcessingException e) {
				throw new HttpException("Jackson 解析异常：" + json, e);
			}
		}
		return null;
	}
	
	
	public static class User {
		
		private int id;
		private String name;
		private Date date = new Date();
		
		public User() {
		}
		
		public User(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + ", date=" + date + "]";
		}
		
	}
	
}
