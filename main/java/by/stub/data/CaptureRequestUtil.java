package by.stub.data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alemieux on 2/26/14.
 */
public class CaptureRequestUtil {
    public static ConcurrentHashMap<Object,Object> requestCaptured = new ConcurrentHashMap<Object, Object>();

    public static void capture(Object key, Object value){
        requestCaptured.put(key,value);
    }

    public static String getRequests(){
        return requestCaptured.toString();
    }

    public static Object get(Object key){
        return requestCaptured.get(key);
    }
}
