package by.stub.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.StubCallback;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
final class StubCallbackBuilder implements StubBuilder<StubCallback> {

   private Map<String, Object> fieldNameAndValues;
   private String method;
   private String status;
   private String body;
   private String url;
   private String latency;
   private Map<String, String> headers;

   StubCallbackBuilder() {
	  this.method = null;
      this.status = null;
      this.body = null;
      this.url = null;
      this.latency = null;
      this.headers = new LinkedHashMap<String, String>();
      this.fieldNameAndValues = new HashMap<String, Object>();
   }

   @Override
   public void store(final String fieldName, Object fieldValue) {
	  if (fieldName.equalsIgnoreCase(YamlProperties.METHOD)){
		  // Convert from ArrayList to String
		  fieldValue = ((ArrayList<String>) fieldValue).get(0);
	  }	  
      fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
   }

   @Override
   public StubCallback build()  throws Exception {
      ReflectionUtils.injectObjectFields(this, fieldNameAndValues);          
      return new StubCallback(method,status, body, headers,url,latency);    	        
   }
}
