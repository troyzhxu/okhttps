package com.ejlchina.okhttps.gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class TestCase {

	
	Gson gson = new Gson();
	
	@Test
	public void test() {
		
		
		User u1 = new User(1, "Tom");
		User u2 = new User(2, "Jack");
		List<User> list = new ArrayList<>();
		list.add(u1);
		list.add(u2);
		
		String json = gson.toJson(list);
		
		System.out.println("json = " + json);
		
//		User[] users = gson.fromJson(json, TypeToken.getArray(User.class).getType());
		
		List<User> users = jsonToList(User.class, json);
		
		System.out.println(users.get(0));
		System.out.println(users.get(0));
	}
	
	@Test
	public void test2() {
		User u1 = new User(1, "Tom");
		System.out.println(toJsonStr(u1, "yyyy-MM-dd HH:mm:ss"));
	}
	
	@Test
	public void test3() {
		User u1 = new User(1, "Tom");
		String json = gson.toJson(u1);
		System.out.println(json);
		
		JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
		
		System.out.println(jsonObj);
		System.out.println(jsonObj.get("id").getAsInt());
		System.out.println(jsonObj.get("name").getAsString());
	}
	
	@Test
	public void test4() {
		User u1 = new User(1, "Tom");
		User u2 = new User(2, "Jack");
		List<User> list = new ArrayList<>();
		list.add(u1);
		list.add(u2);
		
		String json = gson.toJson(list);
		
		JsonArray jsonArr = gson.fromJson(json, JsonArray.class);
		
		System.out.println(jsonArr);
		System.out.println(jsonArr.get(0));
		System.out.println(jsonArr.get(1));
	}
	
	public <T> List<T> jsonToList(Class<T> type, String json) {
		T[] objs = gson.fromJson(json, TypeToken.getArray(type).getType());
		List<T> list = new ArrayList<>();
		Collections.addAll(list, objs);
		return list;
	}
	
	
	public String toJsonStr(Object bean, String dateFormat) {
		return gson.newBuilder().setDateFormat(dateFormat).create().toJson(bean);
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
