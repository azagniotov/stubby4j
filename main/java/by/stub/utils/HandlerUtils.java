/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.utils;

import by.stub.annotations.CoberturaIgnore;
import by.stub.exception.Stubby4JException;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import nl.flotsam.xeger.Xeger;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:00 AM
 */
@SuppressWarnings("serial")
public final class HandlerUtils {
   private final static String XEGER_EXPRESSION_LABEL = "xeger.";
   private final static String XEGER_EXTRACT_REGEX = ".*<%.*xeger\\.(\\d+):(.*)%>.*";

   private HandlerUtils() {

   }

   public static void configureErrorResponse(final HttpServletResponse response, final int httpStatus, final String message) throws IOException {
      response.setStatus(httpStatus);
      response.sendError(httpStatus, message);
      response.flushBuffer();
   }

   public static String getHtmlResourceByName(final String templateSuffix) {
      final String htmlTemplatePath = String.format("/ui/html/%s.html", templateSuffix);
      final InputStream inputStream = HandlerUtils.class.getResourceAsStream(htmlTemplatePath);
      if (ObjectUtils.isNull(inputStream)) {
         throw new Stubby4JException(String.format("Could not find resource %s", htmlTemplatePath));
      }
      return StringUtils.inputStreamToString(inputStream);
   }

   @CoberturaIgnore
   public static String constructHeaderServerName() {
      final Package pkg = HandlerUtils.class.getPackage();
      final String implementationVersion = StringUtils.isSet(pkg.getImplementationVersion()) ?
         pkg.getImplementationVersion() : "x.x.xx";

      return String.format("stubby4j/%s (HTTP stub server)", implementationVersion);
   }

   public static void setResponseMainHeaders(final HttpServletResponse response) {
      response.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      response.setHeader(HttpHeaders.DATE, new Date().toString());
      response.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT_HTML_UTF_8);
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"); // HTTP 1.1.
      response.setHeader(HttpHeaders.PRAGMA, "no-cache"); // HTTP 1.0.
      response.setDateHeader(HttpHeaders.EXPIRES, 0);
   }

   public static String linkifyRequestUrl(final String scheme, final Object uri, final String host, final int port) {
      final String fullUrl = String.format("%s://%s:%s%s", scheme, host, port, uri);
      final String href = StringUtils.encodeSingleQuotes(fullUrl);
      return String.format("<a target='_blank' href='%s'>%s</a>", href, fullUrl);
   }

   public static String populateHtmlTemplate(final String templateName, final Object... params) {
      final StringBuilder builder = new StringBuilder();
      builder.append(String.format(getHtmlResourceByName(templateName), params));
      return builder.toString();
   }

   public static String extractPostRequestBody(final HttpServletRequest request, final String source) throws IOException {
      final Set<String> httpMethodsContainingBody = new HashSet<String>() {{
         add("post");
         add("put");
      }};

      if (!httpMethodsContainingBody.contains(request.getMethod().toLowerCase())) {
         return null;
      }

      try {
         final String requestContent = StringUtils.inputStreamToString(request.getInputStream());
         return requestContent.replaceAll("\\\\/", "/"); //https://code.google.com/p/snakeyaml/issues/detail?id=93
      } catch (final Exception ex) {
         final String err = String.format("Error when extracting POST body: %s, returning null..", ex.toString());
         ConsoleUtils.logIncomingRequestError(request, source, err);
         return null;
      }
   }

   public static String calculateStubbyUpTime(final long timestamp) {
      final long days = TimeUnit.MILLISECONDS.toDays(timestamp);
      final long hours = TimeUnit.MILLISECONDS.toHours(timestamp) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(timestamp));
      final long mins = TimeUnit.MILLISECONDS.toMinutes(timestamp) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timestamp));
      final long secs = TimeUnit.MILLISECONDS.toSeconds(timestamp) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timestamp));

      return String.format("%d day%s, %d hour%s, %d min%s, %d sec%s",
         days, pluralize(days), hours, pluralize(hours), mins, pluralize(mins), secs, pluralize(secs));
   }

   private static String pluralize(final long timeUnit) {
      return timeUnit == 1 ? "" : "s";
   }

    public static Map<String,String> getXegerTokenWithValues(String value, StubRequest assertionStubRequest){
    	ConcurrentHashMap<String,String> tokenToReplace = new ConcurrentHashMap<String, String>();

        Pattern p = Pattern.compile(XEGER_EXTRACT_REGEX);
        Matcher m = p.matcher(value);
        while (m.find()){
            String index = m.group(1); // The Xeger variable number
            String regex = m.group(2); // the Xeger expression to generate a string
            String generated;
            String key = XEGER_EXPRESSION_LABEL + index + ".*";
            if (ObjectUtils.isNotNull(assertionStubRequest.getXegerVariables().get(key))){
	    		generated = assertionStubRequest.getXegerVariables().get(key);
	    	}else{
	    		Xeger generator = new Xeger(regex);
	            generated = generator.generate();
	         	assertionStubRequest.getXegerVariables().put(key,generated);
	        }            
            tokenToReplace.put(key,generated);
        }         
        return tokenToReplace;
    }
   
    public static HttpServletResponse setStubResponseHeaders(StubResponse stubResponse, HttpServletResponse response, StubRequest assertionStubRequest) {	  
  	  String headerValue;  	  
  	  
      response.setCharacterEncoding(StringUtils.UTF_8);      
      if (ObjectUtils.isNotNull(stubResponse.getHeaders())){    		 
      	  for (Entry<String, String> entry : stubResponse.getHeaders().entrySet()){      
      		headerValue = entry.getValue();      		
            //Check if the header contains a regex Xeger expression
            if (headerValue.contains(StringUtils.TEMPLATE_TOKEN_LEFT)){
                    if (headerValue.toLowerCase().contains("xeger")){                    	
                    	// Get all Xeger placeholder found in the response
                    	Map<String,String> xegerVariables = HandlerUtils.getXegerTokenWithValues(headerValue, assertionStubRequest);                    	
                    	headerValue = StringUtils.replaceTokens(headerValue.getBytes(), xegerVariables);
                    }else {
                    	headerValue = StringUtils.replaceTokens(headerValue.getBytes(), assertionStubRequest.getRegexGroups());	
                    }                    
            }                
            response.setHeader(entry.getKey(), headerValue);              
      	  }    	  
      }        
      return response;
   }
}