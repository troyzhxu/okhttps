package com.ejlchina.test;

import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OnCallback;
import com.ejlchina.okhttps.HttpResult.State;

import org.junit.Test;

public class CacheTests extends BaseTest {

    @Test
    public void testCache() {
        HTTP http = HTTP.builder().build();

        
        
//        
//        
//        HttpResult.Body body = http.sync("http://xxx.cdyun.vip/comm/provinces")
//        		
//                .get()
//                .getBody()
//                .cache();
//
//        println("result = " + body.toString());
//        println("result = " + body.toArray());
//
//        body.close();
//
//        println("result = " + body.toString());
//        println("result = " + body.toArray());
//        
//        
//        
//        
//        http.async("")
//        	.addBodyPara("", "")
//        	
//        	
//        	.setOnResponse((HttpResult res) -> {
//        		
//        		Mapper mapper = res.getBody().toMapper(); 
//        		
//        	})
//        	.setOnComplete(state -> {
//        		
//        		testCache();
//        	})
//        	.setOnProcess(process -> {
//        		
//        		double rate = process.getRate();
//        		
//        	})
//        	.post()
//        	.cancel()
//        	;

        
    }
    
    class OnComplete implements OnCallback<HttpResult.State> {
    	
    	@Override
		public void on(State date) {
			
    		 testCache();
		}
    	
    }

}
