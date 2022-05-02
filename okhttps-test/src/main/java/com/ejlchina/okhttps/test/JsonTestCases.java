package com.ejlchina.okhttps.test;

import com.ejlchina.okhttps.MsgConvertor;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;


public class JsonTestCases extends BaseTestCases {

	public JsonTestCases(MsgConvertor msgConvertor) {
		super(msgConvertor);
	}

	@Override
	public void run() throws Exception {
		super.run();
		testSerializeList();
	}

	@Override
	String getUser1Str() {
		return "{\"id\":1,\"name\":\"Jack\"}";
	}

	@Override
	String getUser1ResultStr() {
		return "{\"code\": 200, \"data\": {\"id\":1,\"name\":\"Jack\"}, \"msg\": \"ok\"}";
	}

	@Override
	String getUserListStr() {
		return "[{\"id\":1,\"name\":\"Jack\"},{\"id\":2,\"name\":\"Tom\"}]";
	}

	void testSerializeList() {
		User u1 = new User(1, "Jack");
		User u2 = new User(2, "Tom");
		User[] list = new User[] {u1, u2};
		byte[] data = msgConvertor.serialize(list, StandardCharsets.UTF_8);
		String json = new String(data, StandardCharsets.UTF_8);
		Assert.assertEquals(getUserListStr(), json);
		System.out.println("case 4 passed!");
	}

}
