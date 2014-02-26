package by.stub.handlers.strategy.stubs.callback;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import by.stub.client.StubbyClient;
import by.stub.client.StubbyResponse;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubCallback;
import by.stub.yaml.stubs.StubRequest;

public class DefaultCallbackHandlingStrategy implements
		StubCallbackHandlingStrategy {

	@Override
	public void handle(final StubCallback callbackRequest,
			final StubRequest assertionStubRequest) throws Exception {
		// We spawn a thread that will send a response in X milliseconds
		(new Thread() {
			public void run() {
				StubbyClient client = new StubbyClient();													
				try {										
					// CONSTRUCT CALLBACK REQUEST									
					// Setup Callback URL
					final String callbackUrl = StringUtils.replaceTokens(callbackRequest.getUrl().getBytes(), assertionStubRequest.getRegexGroups());												        
					URL url = new URL(callbackUrl);						
											
					// Setup Request BODY
				    byte[] responseBody = callbackRequest.getResponseBodyAsBytes();
				    if (callbackRequest.isContainsTemplateTokens()) {
				       final String replacedTemplate = StringUtils.replaceTokens(responseBody, assertionStubRequest.getRegexGroups());
				       responseBody = StringUtils.getBytesUtf8(HandlerUtils.processXegerVariables(replacedTemplate));
				    }

				    // Setup Request HEADERS
				    Map<String,String> headers = callbackRequest.getHeaders();				    
				    
				    // Apply latency
				    TimeUnit.MILLISECONDS.sleep(Long.parseLong(callbackRequest.getLatency()));
				    
				    // SEND REQUEST
				    StubbyResponse response = client.makeRequest(url.getProtocol(),callbackRequest.getMethod(), url.getHost(), url.getPath(), url.getPort(), new String(responseBody),headers);//
					ConsoleUtils.logOutgoingCallback(url.toString(), callbackRequest, new String(responseBody));
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		}).start();		
	}

}
