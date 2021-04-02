package io.github.azagniotov.stubby4j.yaml;

import io.github.azagniotov.stubby4j.common.Common;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;


public class YamlBuilderTest {

    @Test
    public void shouldBuildStubbedResponseWithSequenceResponses() throws Exception {
        final String expectedYaml =
                "-  description: Hello!" + BR +
                        "   uuid: abc-123-def-456" + BR +
                        "   request:" + BR +
                        "      method: [PUT]" + BR +
                        "      url: /invoice" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      -  status: 200" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         body: OK" + BR +
                        "" + BR +
                        "      -  status: 200" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         body: Still going strong!" + BR +
                        "" + BR +
                        "      -  status: 500" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         body: OMFG!!!" + BR +
                        "         file: ../../response.json";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder
                .newStubbedFeature()
                .withDescription("Hello!")
                .withUUID("abc-123-def-456")
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OK")
                .withLineBreak()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("Still going strong!")
                .withLineBreak()
                .withSequenceResponseStatus("500")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OMFG!!!")
                .withSequenceResponseFile("../../response.json")
                .withLineBreak()
                .build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }

    @Test
    public void shouldBuildStubbedResponseWithSequenceResponsesFoldedBodyAndFile() throws Exception {
        final String expectedYaml =
                "-  request:" + BR +
                        "      method: [PUT]" + BR +
                        "      url: /invoice" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      -  status: 200" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         body: OK" + BR +
                        "" + BR +
                        "      -  status: 200" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         body: >" + BR +
                        "            {\"status\", \"200\"}" + BR +
                        "" + BR +
                        "      -  status: 500" + BR +
                        "         headers: " + BR +
                        "            content-type: application/json" + BR +
                        "         file: ../path/to/error.file";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OK")
                .withLineBreak()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseFoldedBody("{\"status\", \"200\"}")
                .withLineBreak()
                .withSequenceResponseStatus("500")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseFile("../path/to/error.file")
                .withLineBreak()
                .build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }

    @Test
    public void shouldBuildStubbedRequestWithMultipleMethods() throws Exception {
        final String expectedYaml =
                "-  request:" + BR +
                        "      method: [HEAD, GET, PUT]" + BR +
                        "      url: /invoice" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      status: 200" + BR +
                        "      body: OK";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder.
                newStubbedRequest().
                withMethodHead().
                withMethodGet().
                withMethodPut().
                withUrl("/invoice").
                newStubbedResponse().
                withStatus("200").
                withLiteralBody("OK").build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }

    @Test
    public void shouldBuildStubbedRequestWithStubbedResponse() throws Exception {
        final String expectedYaml =
                "-  request:" + BR +
                        "      query:" + BR +
                        "         status: active" + BR +
                        "         type: full" + BR +
                        "      method: [GET]" + BR +
                        "      url: /invoice" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      headers:" + BR +
                        "         content-type: application/json" + BR +
                        "         pragma: no-cache" + BR +
                        "      status: 200" + BR +
                        "      file: ../json/systemtest-body-response-as-file.json";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder.
                newStubbedRequest().
                withQuery("status", "active").
                withQuery("type", "full").
                withMethodGet().
                withUrl("/invoice").
                newStubbedResponse().
                withHeaders("content-type", Common.HEADER_APPLICATION_JSON).
                withHeaders("pragma", "no-cache").
                withStatus("200").
                withFile("../json/systemtest-body-response-as-file.json").build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }


    @Test
    public void shouldBuildStubbedRequestWithMultilineStubbedResponse() throws Exception {

        final String expectedYaml =
                "-  request:" + BR +
                        "      method: [PUT]" + BR +
                        "      url: /invoice/123" + BR +
                        "      headers:" + BR +
                        "         content-type: application/json" + BR +
                        "      post: >" + BR +
                        "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      headers:" + BR +
                        "         content-type: application/json" + BR +
                        "         pragma: no-cache" + BR +
                        "      status: 200" + BR +
                        "      body: >" + BR +
                        "         {\"id\": \"123\", \"status\": \"updated\"}";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder.
                newStubbedRequest().
                withMethodPut().
                withUrl("/invoice/123").
                withHeaders("content-type", Common.HEADER_APPLICATION_JSON).
                withFoldedPost("{\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}").
                newStubbedResponse().
                withHeaders("content-type", Common.HEADER_APPLICATION_JSON).
                withHeaders("pragma", "no-cache").
                withStatus("200").
                withFoldedBody("{\"id\": \"123\", \"status\": \"updated\"}").build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }


    @Test
    public void shouldBuildStubbedRequestWithStubbedResponseWhenBothHaveManyHeaders() throws Exception {
        final String expectedYaml =
                "-  request:" + BR +
                        "      headers:" + BR +
                        "         content-type: application/json" + BR +
                        "         content-language: US-en" + BR +
                        "         content-encoding: gzip" + BR +
                        "         content-length: 30" + BR +
                        "         " + BASIC.asYAMLProp() + ": bob:secret" + BR +
                        "         " + BEARER.asYAMLProp() + ": jkRUTBUjghbjtUGT==" + BR +
                        "         " + CUSTOM.asYAMLProp() + ": Custom jkRUTBUjghbjtUGT==" + BR +
                        "      method: [GET]" + BR +
                        "      url: /invoice" + BR +
                        "" + BR +
                        "   response:" + BR +
                        "      headers:" + BR +
                        "         content-type: application/json" + BR +
                        "         content-language: US-en" + BR +
                        "         content-encoding: gzip" + BR +
                        "         content-length: 30" + BR +
                        "         pragma: no-cache" + BR +
                        "         location: /invoice/exit";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder.
                newStubbedRequest().
                withHeaderContentType(Common.HEADER_APPLICATION_JSON).
                withHeaderContentLanguage("US-en").
                withHeaderContentEncoding("gzip").
                withHeaderContentLength("30").
                withHeaderAuthorizationBasic("bob:secret").
                withHeaderAuthorizationBearer("jkRUTBUjghbjtUGT==").
                withHeaderAuthorizationCustom("Custom jkRUTBUjghbjtUGT==").
                withMethodGet().
                withUrl("/invoice").
                newStubbedResponse().
                withHeaderContentType(Common.HEADER_APPLICATION_JSON).
                withHeaderContentLanguage("US-en").
                withHeaderContentEncoding("gzip").
                withHeaderContentLength("30").
                withHeaderPragma("no-cache").
                withHeaderLocation("/invoice/exit").build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }

    @Test
    public void shouldBuildStubbedProxyConfig() throws Exception {
        final String expectedYaml =
                "-  proxy-config:" + BR +
                        "      description: very-interesting-description" + BR +
                        "      uuid: very-unique-name" + BR +
                        "      strategy: as-is" + BR +
                        "      headers:" + BR +
                        "         keyOne: valueOne" + BR +
                        "         keyTwo: valueTwo" + BR +
                        "      properties:" + BR +
                        "         endpoint: http://google.com" + BR +
                        "         key: value";

        final YamlBuilder YamlBuilder = new YamlBuilder();
        final String actualYaml = YamlBuilder
                .newStubbedProxyConfig()
                .withDescription("very-interesting-description")
                .withUuid("very-unique-name")
                .withProxyStrategyAsIs()
                .withHeader("keyOne", "valueOne")
                .withHeader("keyTwo", "valueTwo")
                .withPropertyEndpoint("http://google.com")
                .withProperty("key", "value")
                .toString().trim();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }
}
