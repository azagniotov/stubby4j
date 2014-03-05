package by.stub.yaml.stubs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import by.stub.annotations.CoberturaIgnore;
import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;

public class StubCallback {
	   private final String method;
	   private final String status;
	   private final String body;
	   private final String url;
	   private final String latency;
	   private final Map<String, String> headers;	   

public StubCallback(String method, String status, String body, Map<String, String> headers, String url, String latency) {		
	this.method = method;
	this.status = status;
	this.body = body;
	this.headers = headers;
	this.url = url;
	this.latency = ObjectUtils.isNull(latency) ? "0" : latency;
}

public String getScheme() throws MalformedURLException{
	URL url = new URL(this.url);	
	return url.getProtocol();	
}

public String getHost() throws MalformedURLException {	
	URL url = new URL(this.url);	
	return url.getHost();	
}

public String getMethod(){
	return method;
}

public String getStatus() {
	return status;
}

public String getBody() {
	return body;
}

public String getUrl() {
	return url;
}

public String getLatency(){
	return latency;
}

public Map<String, String> getHeaders() {
	return headers;
}

public byte[] getResponseBodyAsBytes() {   
    return getBody().getBytes(StringUtils.charsetUTF8());    
 }

 public boolean isContainsTemplateTokens() {
    final boolean isFileTemplate = false; 
//    		fileBytes.length == 0 ? false : isTemplateFile();
    return isFileTemplate || getBody().contains(StringUtils.TEMPLATE_TOKEN_LEFT);
 }

public String getUri() throws MalformedURLException {
	URL url = new URL(this.url);	
	return url.getPath();	
}

public int getPort() throws MalformedURLException {
	URL url = new URL(this.url);	
	return url.getPort();	
}

 @Override
 @CoberturaIgnore
 public int hashCode() {
    int result = (ObjectUtils.isNotNull(url) ? url.hashCode() : 0);
    result = 31 * result + method.hashCode();
    result = 31 * result + (ObjectUtils.isNotNull(body) ? body.hashCode() : 0);    
    result = 31 * result + headers.hashCode();    
    return result;
 }

 @Override
 @CoberturaIgnore
 public final String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("StubCallback");
    sb.append("{url=").append(url);
    sb.append(", method=").append(method);

    if (!ObjectUtils.isNull(body)) {
       sb.append(", post=").append(body);
    }

    sb.append(", headers=").append(getHeaders());
    sb.append('}');

    return sb.toString();
 }
 
}
