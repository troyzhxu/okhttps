package cn.zhxu.okhttps;

import cn.zhxu.okhttps.HTTP;
import okhttp3.mockwebserver.MockWebServer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseTest {

	protected MockWebServer server = new MockWebServer();
	
	protected String mockUrl = "http://" + server.getHostName() + ":" + server.getPort();

    protected HTTP http = HTTP.builder().baseUrl(mockUrl).build();


    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void println() {
        System.out.println();
    }

    public static void println(Object x) {
        System.out.println(x);
    }

    public static void println(long t0, String str) {
        System.out.println(now() - t0 + "\t" + str);
    }
    
    
    public static void log(String str) {
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    	System.out.println(sdf.format(new Date()) + "\t" + str);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

}
