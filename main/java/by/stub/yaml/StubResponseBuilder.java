package by.stub.yaml;

import by.stub.utils.ReflectionUtils;
import by.stub.yaml.stubs.StubCallback;
import by.stub.yaml.stubs.StubResponse;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 4/14/13, 4:54 PM
 */
final class StubResponseBuilder implements StubBuilder<StubResponse> {

	private Map<String, Object> fieldNameAndValues;
	private String status;
	private String body;
	private File file;
	private String latency;
	private Map<String, String> headers;
	private StubCallback callback;
    private String capture;

	StubResponseBuilder() {
		initialize();
	}

	@Override
	public void store(final String fieldName, final Object fieldValue) {
		fieldNameAndValues.put(fieldName.toLowerCase(), fieldValue);
	}

	private void initialize() {
		this.status = null;
		this.body = null;
		this.file = null;
		this.latency = null;
		this.headers = new LinkedHashMap<String, String>();
		this.callback = null;
		this.fieldNameAndValues = new HashMap<String, Object>();
        this.capture = "false";
	}

	@Override
	public StubResponse build() throws Exception {
		ReflectionUtils.injectObjectFields(this, fieldNameAndValues);
		StubResponse result = new StubResponse(status, body, file, latency,
				headers, callback, capture);
		initialize();
		return result;
	}
}
