package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;

public class YamlParserLoadTest {

    private static final YamlBuilder YAML_BUILDER = new YamlBuilder();


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

        final YamlParseResultSet yamlParseResultSet = loadYamlToDataStore(rawYaml);
        assertThat(yamlParseResultSet.getStubs().size()).isEqualTo(NUMBER_OF_HTTPCYCLES);

        final StubHttpLifecycle actualHttpLifecycle = yamlParseResultSet.getStubs().get(498);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualRequest.getUrl()).contains(String.format("%s/%s", baseRequestUrl, 499));
        assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamOne, 499));
        assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamTwo, 499));

        assertThat(actualResponse.getHeaders()).containsEntry(expectedHeaderKey, expectedHeaderValue);
    }

    private YamlParseResultSet loadYamlToDataStore(final String yaml) throws Exception {
        return new YamlParser().parse(".", yaml);
    }

}
