package io.github.azagniotov.stubby4j.test.builders.yaml;

import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.utils.FileUtils;
import org.junit.Test;

import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BASIC;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.BEARER;
import static io.github.azagniotov.stubby4j.yaml.stubs.StubAuthorizationTypes.CUSTOM;
import static org.fest.assertions.api.Assertions.assertThat;


/**
 * @author Alexander Zagniotov
 * @since 4/13/13, 12:50 AM
 */
public class YamlBuilderTest {

    @Test
    public void shouldBuildStubbedResponseWithSequenceResponses() throws Exception {
        final String expectedYaml =
                "-  request:" + FileUtils.BR +
                        "      method: [PUT]" + FileUtils.BR +
                        "      url: /invoice" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      -  status: 200" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         body: OK" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "      -  status: 200" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         body: Still going strong!" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "      -  status: 500" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         body: OMFG!!!" + FileUtils.BR +
                        "         file: ../../response.json";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder
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
                "-  request:" + FileUtils.BR +
                        "      method: [PUT]" + FileUtils.BR +
                        "      url: /invoice" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      -  status: 200" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         body: OK" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "      -  status: 200" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         body: >" + FileUtils.BR +
                        "            {\"status\", \"200\"}" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "      -  status: 500" + FileUtils.BR +
                        "         headers: " + FileUtils.BR +
                        "            content-type: application/json" + FileUtils.BR +
                        "         file: ../path/to/error.file";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder
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
                "-  request:" + FileUtils.BR +
                        "      method: [HEAD, GET, PUT]" + FileUtils.BR +
                        "      url: /invoice" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      status: 200" + FileUtils.BR +
                        "      body: OK";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder.
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
                "-  request:" + FileUtils.BR +
                        "      query:" + FileUtils.BR +
                        "         status: active" + FileUtils.BR +
                        "         type: full" + FileUtils.BR +
                        "      method: [GET]" + FileUtils.BR +
                        "      url: /invoice" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      headers:" + FileUtils.BR +
                        "         content-type: application/json" + FileUtils.BR +
                        "         pragma: no-cache" + FileUtils.BR +
                        "      status: 200" + FileUtils.BR +
                        "      file: ../json/systemtest-body-response-as-file.json";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder.
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
                "-  request:" + FileUtils.BR +
                        "      method: [PUT]" + FileUtils.BR +
                        "      url: /invoice/123" + FileUtils.BR +
                        "      headers:" + FileUtils.BR +
                        "         content-type: application/json" + FileUtils.BR +
                        "      post: >" + FileUtils.BR +
                        "         {\"name\": \"milk\", \"description\": \"full\", \"department\": \"savoury\"}" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      headers:" + FileUtils.BR +
                        "         content-type: application/json" + FileUtils.BR +
                        "         pragma: no-cache" + FileUtils.BR +
                        "      status: 200" + FileUtils.BR +
                        "      body: >" + FileUtils.BR +
                        "         {\"id\": \"123\", \"status\": \"updated\"}";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder.
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
                "-  request:" + FileUtils.BR +
                        "      headers:" + FileUtils.BR +
                        "         content-type: application/json" + FileUtils.BR +
                        "         content-language: US-en" + FileUtils.BR +
                        "         content-length: 30" + FileUtils.BR +
                        "         " + BASIC.asYamlProp() + ": bob:secret" + FileUtils.BR +
                        "         " + BEARER.asYamlProp() + ": jkRUTBUjghbjtUGT==" + FileUtils.BR +
                        "         " + CUSTOM.asYamlProp() + ": Custom jkRUTBUjghbjtUGT==" + FileUtils.BR +
                        "      method: [GET]" + FileUtils.BR +
                        "      url: /invoice" + FileUtils.BR +
                        "" + FileUtils.BR +
                        "   response:" + FileUtils.BR +
                        "      headers:" + FileUtils.BR +
                        "         content-type: application/json" + FileUtils.BR +
                        "         content-language: US-en" + FileUtils.BR +
                        "         content-length: 30" + FileUtils.BR +
                        "         pragma: no-cache" + FileUtils.BR +
                        "         location: /invoice/exit";

        final YamlBuilder yamlBuilder = new YamlBuilder();
        final String actualYaml = yamlBuilder.
                newStubbedRequest().
                withHeaderContentType(Common.HEADER_APPLICATION_JSON).
                withHeaderContentLanguage("US-en").
                withHeaderContentLength("30").
                withHeaderAuthorizationBasic("bob:secret").
                withHeaderAuthorizationBearer("jkRUTBUjghbjtUGT==").
                withHeaderAuthorizationCustom("Custom jkRUTBUjghbjtUGT==").
                withMethodGet().
                withUrl("/invoice").
                newStubbedResponse().
                withHeaderContentType(Common.HEADER_APPLICATION_JSON).
                withHeaderContentLanguage("US-en").
                withHeaderContentLength("30").
                withHeaderPragma("no-cache").
                withHeaderLocation("/invoice/exit").build();

        assertThat(actualYaml).isEqualTo(expectedYaml);

    }
}