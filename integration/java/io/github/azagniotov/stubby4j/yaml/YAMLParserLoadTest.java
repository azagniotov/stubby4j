package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.builder.yaml.YAMLBuilder;
import io.github.azagniotov.stubby4j.yaml.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.yaml.stubs.StubRequest;
import io.github.azagniotov.stubby4j.yaml.stubs.StubResponse;
import org.fest.assertions.data.MapEntry;
import org.junit.Test;

import java.util.List;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author: Alexander Zagniotov
 * Created: 4/22/13 6:39 PM
 */
public class YAMLParserLoadTest {

    private static final YAMLBuilder YAML_BUILDER = new YAMLBuilder();


    @Test
    public void loadTest_shouldUnmarshallHugeYamlIntoObjectTree_WhenYAMLValid() throws Exception {

        final String baseRequestUrl = "/some/uri";

        final String expectedHeaderKey = "location";
        final String expectedHeaderValue = "/invoice/123";

        final String expectedParamOne = "paramOne";
        final String expectedParamTwo = "paramTwo";

        final String stubbedResponseBody = "Hello, this is a response body";
        final String stubbedResponseStatus = "301";

        final int NUMBER_OF_HTTPCYCLES = 500;
        final StringBuilder BUILDER = new StringBuilder(128);

        for (int idx = 1; idx <= NUMBER_OF_HTTPCYCLES; idx++) {
            String expectedUrl = String.format("%s/%s", baseRequestUrl, idx);

            String yaml = YAML_BUILDER.newStubbedRequest()
                    .withMethodGet()
                    .withUrl(expectedUrl)
                    .withQuery(expectedParamOne, String.valueOf(idx))
                    .withQuery(expectedParamTwo, String.valueOf(idx))
                    .newStubbedResponse()
                    .withStatus(stubbedResponseStatus)
                    .withHeaders(expectedHeaderKey, expectedHeaderValue)
                    .withLiteralBody(stubbedResponseBody).build();

            BUILDER.append(yaml).append(BR).append(BR);
        }

        BUILDER.trimToSize();
        final String rawYaml = BUILDER.toString();
        BUILDER.setLength(0);

        final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(rawYaml);
        assertThat(loadedHttpCycles.size()).isEqualTo(NUMBER_OF_HTTPCYCLES);

        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(498);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualRequest.getUrl()).contains(String.format("%s/%s", baseRequestUrl, 499));
        assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamOne, 499));
        assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamTwo, 499));

        final MapEntry headerEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
        assertThat(actualResponse.getHeaders()).contains(headerEntry);
    }

    private List<StubHttpLifecycle> loadYamlToDataStore(final String yaml) throws Exception {
        return new YAMLParser().parse(".", yaml);
    }

}
