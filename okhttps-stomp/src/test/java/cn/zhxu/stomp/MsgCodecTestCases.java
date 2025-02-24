package cn.zhxu.stomp;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class MsgCodecTestCases {

	static class DecodeCallback implements Consumer<Message> {

		private final List<Message> list = new ArrayList<>();

		@Override
		public void accept(Message data) {
			list.add(data);
		}

		public List<Message> getList() {
			return list;
		}
	}


	@Test
    public void testMsgCodecImplDecode1() {
		MsgCodec msgCodec = new MsgCodecImpl();
		DecodeCallback callback = new DecodeCallback();
		msgCodec.decode("MESSAGE\nk1:v1\nk2:v2\n\n123456\u0000", callback);
		List<Message> list = callback.getList();
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals("MESSAGE", message.getCommand());
		Assert.assertEquals("123456", message.getPayload());
		Assert.assertEquals("v1", message.headerValue("k1"));
		Assert.assertEquals("v2", message.headerValue("k2"));
    }

	@Test
	public void testMsgCodecImplDecode2() {
		MsgCodec msgCodec = new MsgCodecImpl();
		DecodeCallback callback = new DecodeCallback();
		msgCodec.decode(" \n MESSAGE\nk1:v1\nk2:v2\n\n123456\u0000", callback);
		List<Message> list = callback.getList();
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals("MESSAGE", message.getCommand());
		Assert.assertEquals("123456", message.getPayload());
		Assert.assertEquals("v1", message.headerValue("k1"));
		Assert.assertEquals("v2", message.headerValue("k2"));
	}

	@Test
	public void testMsgCodecImplDecode3() {
		MsgCodec msgCodec = new MsgCodecImpl();
		DecodeCallback callback = new DecodeCallback();
		msgCodec.decode(" \n ", callback);
		msgCodec.decode("  MESSAGE\nk1:", callback);
		msgCodec.decode("v1\nk2:v2\n\n12345", callback);
		msgCodec.decode("6\u0000", callback);
		msgCodec.decode("\n\nMS", callback);
		msgCodec.decode("\n\nMES", callback);
		msgCodec.decode("SAGE\nk1:", callback);
		msgCodec.decode("v1\nk2:v2\n\n12345", callback);
		msgCodec.decode("6\u0000", callback);
		List<Message> list = callback.getList();
		Assert.assertEquals(2, list.size());
		Message message = list.get(1);
		Assert.assertEquals("MESSAGE", message.getCommand());
		Assert.assertEquals("123456", message.getPayload());
		Assert.assertEquals("v1", message.headerValue("k1"));
		Assert.assertEquals("v2", message.headerValue("k2"));
	}

}
