package io.github.azagniotov.stubby4j.yaml;

import com.google.api.client.http.HttpMethods;
import io.github.azagniotov.stubby4j.common.Common;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubProxyStrategy;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.StringUtils.inputStreamToString;
import static org.junit.Assert.assertThrows;


public class YamlParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private YamlBuilder yamlBuilder;

    @Before
    public void setUp() throws Exception {
        yamlBuilder = new YamlBuilder();
    }

    @Test
    public void shouldThrow_WhenEmptyYAMLGiven() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML");

        unmarshall("");
    }

    @Test
    public void shouldThrow_WhenDuplicatedUuidSpecified() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Stubs YAML contains duplicate UUIDs: 9136d8b7-f7a7-478d-97a5-53292484aaf6");

        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/duplicated.uuid.stub.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
    }

    @Test
    public void shouldThrow_WhenRequestYAMLContainsUnknownProperty() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("An unknown property configured: methodd");

        final String yaml =
                "-  request:\n" +
                        "      methodd: [PUT]\n" +
                        "      url: /invoice\n" +
                        "\n" +
                        "   response:\n" +
                        "      status: 200";

        unmarshall(yaml);
    }

    @Test
    public void shouldThrow_WhenResponseYAMLContainsUnknownProperty() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("An unknown property configured: statuss");

        final String yaml =
                "-  request:\n" +
                        "      method: [PUT]\n" +
                        "      url: /invoice\n" +
                        "\n" +
                        "   response:\n" +
                        "      statuss: 200";

        unmarshall(yaml);
    }

    @Test
    public void shouldThrow_WhenResponseListYAMLContainsUnknownProperty() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("An unknown property configured: bodyy");

        final String yaml =
                "-  request:\n" +
                        "      method: [PUT]\n" +
                        "      url: /invoice\n" +
                        "\n" +
                        "   response:\n" +
                        "      - status: 200\n" +
                        "        body: OK\n" +
                        "\n" +
                        "      - status: 200\n" +
                        "        bodyy: OK\n" +
                        "\n" +
                        "      - status: 200\n" +
                        "        body: OK";

        unmarshall(yaml);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithNoProperties() throws Exception {

        final String yaml = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

        assertThat(actualHttpLifecycle.getResponses()).hasSize(1);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithNoSequenceResponses() throws Exception {

        final String expectedStatus = "301";
        final String yaml = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualHttpLifecycle.getResponses()).hasSize(1);
        assertThat(actualResponse.getHttpStatusCode()).isEqualTo(HttpStatus.getCode(Integer.parseInt(expectedStatus)));
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithOneSequenceResponse() throws Exception {

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseStatus = "200";
        final String sequenceResponseBody = "OK";

        final String yaml = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody(sequenceResponseBody)
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse).isInstanceOf(StubResponse.class);
        assertThat(actualResponse.getHeaders()).containsEntry(sequenceResponseHeaderKey, Common.HEADER_APPLICATION_JSON);
        assertThat(actualResponse.getHttpStatusCode()).isEqualTo(HttpStatus.getCode(Integer.parseInt(sequenceResponseStatus)));
        assertThat(actualResponse.getBody()).isEqualTo(sequenceResponseBody);
    }


    @Test
    public void shouldUnmarshall_AndReturnResponsesInSequence_WithManySequenceResponse() throws Exception {

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = "application/xml";
        final String sequenceResponseStatus = "500";
        final String sequenceResponseBody = "OMFG";

        final String yaml = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OK")
                .withLineBreak()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseFoldedBody(sequenceResponseBody)
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

        final StubResponse irrelevantSequenceResponse = actualHttpLifecycle.getResponse(true);
        final StubResponse actualSequenceResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualSequenceResponse).isInstanceOf(StubResponse.class);
        assertThat(actualSequenceResponse.getHeaders()).containsEntry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
        assertThat(actualSequenceResponse.getHttpStatusCode()).isEqualTo(HttpStatus.getCode(Integer.parseInt(sequenceResponseStatus)));
        assertThat(actualSequenceResponse.getBody()).isEqualTo(sequenceResponseBody);
    }


    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithUrlAsRegex() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";
        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getUrl()).isEqualTo(url);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithDescription() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";
        final String yaml = yamlBuilder
                .newStubbedFeature()
                .withDescription("wobble")
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

        assertThat(actualHttpLifecycle.getDescription()).isEqualTo("wobble");
        assertThat(actualHttpLifecycle.getUUID()).isNull();
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithUUID() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";
        final String yaml = yamlBuilder
                .newStubbedFeature()
                .withUUID("9136d8b7-f7a7-478d-97a5-53292484aaf6")
                .withDescription("wobble")
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

        assertThat(actualHttpLifecycle.getUUID()).isEqualTo("9136d8b7-f7a7-478d-97a5-53292484aaf6");
    }

    @Test
    public void shouldUnmarshall_toCompleteYaml_WithUUIDAndDescription() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";
        final String yaml = yamlBuilder
                .newStubbedFeature()
                .withUUID("9136d8b7-f7a7-478d-97a5-53292484aaf6")
                .withDescription("wobble")
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

        assertThat(actualHttpLifecycle.getCompleteYaml()).isEqualTo(
                "- uuid: 9136d8b7-f7a7-478d-97a5-53292484aaf6\n" +
                        "  description: wobble\n" +
                        "  request:\n" +
                        "    method:\n" +
                        "    - GET\n" +
                        "    url: ^/[a-z]{3}/[0-9]+/?$\n" +
                        "  response:\n" +
                        "    status: 301\n");
    }

    @Test
    public void shouldUnmarshall_toCompleteYamlFromFile_WithUUIDAndDescription() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/feature.stub.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        final YamlParseResultSet yamlParseResultSet = new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
        final StubHttpLifecycle actualHttpLifecycle = yamlParseResultSet.getStubs().get(0);

        assertThat(actualHttpLifecycle.getCompleteYaml()).isEqualTo(
                "- description: Stub one\n" +
                        "  uuid: 9136d8b7-f7a7-478d-97a5-53292484aaf6\n" +
                        "  request:\n" +
                        "    url: ^/one$\n" +
                        "    method: GET\n" +
                        "  response:\n" +
                        "    status: 200\n" +
                        "    latency: 100\n" +
                        "    body: One!\n");

        assertThat(actualHttpLifecycle.getDescription()).isEqualTo("Stub one");
        assertThat(actualHttpLifecycle.getUUID()).isEqualTo("9136d8b7-f7a7-478d-97a5-53292484aaf6");
    }

    @Test
    public void shouldUnmarshall_toCompleteYamlFromFile() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/feature.stub.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final Object rawYamlConfig = new YamlParser().loadRawYamlConfig(stubsConfigStream);

        assertThat(rawYamlConfig).isInstanceOf(List.class);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithMultipleHTTPMethods() throws Exception {

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withMethodHead()
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getMethod()).containsExactly(HttpMethods.GET, HttpMethods.HEAD);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithDefaultHTTPResponseStatus() throws Exception {

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .newStubbedResponse()
                .withLiteralBody("hello").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse.getHttpStatusCode()).isEqualTo(Code.OK);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithFoldedPost() throws Exception {

        final String stubbedRequestPost = "{\"message\", \"Hello, this is a request post\"}";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withFoldedPost(stubbedRequestPost)
                .newStubbedResponse()
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithFileFailedToLoadAndPostSet() throws Exception {

        final String stubbedRequestFile = "../../very.big.soap.request.xml";

        final String expectedPost = "{\"message\", \"Hello, this is HTTP request post\"}";
        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withFile(stubbedRequestFile)
                .withFoldedPost(expectedPost)
                .newStubbedResponse()
                .withLiteralBody("OK")
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();

        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getFile()).isEqualTo(new byte[]{});
        assertThat(actualRequest.getPostBody()).isEqualTo(expectedPost);
    }

    @Test
    public void shouldCaptureConsoleErrorOutput_WhenYAMLValid_WithFileFailedToLoadAndPostSet() throws Exception {

        final String stubbedRequestFile = "../../very.big.soap.request.xml";

        final String expectedPost = "{\"message\", \"Hello, this is HTTP request post\"}";
        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withFile(stubbedRequestFile)
                .withFoldedPost(expectedPost)
                .newStubbedResponse()
                .withLiteralBody("OK")
                .withStatus("201").build();

        final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        unmarshall(yaml);

        System.setOut(System.out);

        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains("Could not load file from path: ../../very.big.soap.request.xml");
        assertThat(actualConsoleOutput).contains(YamlParser.FAILED_TO_LOAD_FILE_ERR);
    }


    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithFileFailedToLoadAndBodySet() throws Exception {

        final String stubbedResponseFile = "../../very.big.soap.response.xml";

        final String expectedBody = "{\"message\", \"Hello, this is HTTP response body\"}";
        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withFoldedBody(expectedBody)
                .withFile(stubbedResponseFile)
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();

        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse.getFile()).isEqualTo(new byte[]{});
        assertThat(StringUtils.newStringUtf8(actualResponse.getResponseBodyAsBytes())).isEqualTo(expectedBody);
    }

    @Test
    public void shouldCaptureConsoleErrorOutput_WhenYAMLValid_WithFileFailedToLoadAndBodySet() throws Exception {

        final String stubbedResponseFile = "../../very.big.soap.response.xml";

        final String expectedBody = "{\"message\", \"Hello, this is HTTP response body\"}";
        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withFoldedBody(expectedBody)
                .withFile(stubbedResponseFile)
                .withStatus("201").build();

        final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        unmarshall(yaml);

        System.setOut(System.out);

        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).contains("Could not load file from path: ../../very.big.soap.response.xml");
        assertThat(actualConsoleOutput).contains(YamlParser.FAILED_TO_LOAD_FILE_ERR);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithLiteralPost() throws Exception {

        final String stubbedRequestPost = "Hello, this is a request post";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withLiteralPost(stubbedRequestPost)
                .newStubbedResponse()
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
    }

    @Test
    public void shouldFailUnmarshallYaml_WithJsonAsLiteralPost() throws Exception {

        expectedException.expect(ClassCastException.class);
        expectedException.expectMessage("Expected: java.lang.String, instead got: java.util.LinkedHashMap");

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withLiteralPost("{\"message\", \"Hello, this is a request body\"}")
                .newStubbedResponse()
                .withStatus("201").build();

        unmarshall(yaml);
    }


    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithFoldedBody() throws Exception {

        final String stubbedResponseBody = "{\"message\", \"Hello, this is a response body\"}";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withFoldedBody(stubbedResponseBody).build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
    }

    @Test
    public void shouldFailUnmarshallYaml_WithJsonAsLiteralBody() throws Exception {

        expectedException.expect(ClassCastException.class);

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withLiteralBody("{\"message\", \"Hello, this is a response body\"}").build();

        unmarshall(yaml);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithLiteralBody() throws Exception {

        final String stubbedResponseBody = "This is a sentence";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withLiteralBody(stubbedResponseBody).build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
    }


    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithMultipleQueryParams() throws Exception {

        final String expectedParamOne = "paramOne";
        final String expectedParamOneValue = "one";
        final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

        final String expectedParamTwo = "paramTwo";
        final String expectedParamTwoValue = "two";
        final String fullQueryTwo = String.format("%s=%s", expectedParamTwo, expectedParamTwoValue);

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withQuery(expectedParamOne, expectedParamOneValue)
                .withQuery(expectedParamTwo, expectedParamTwoValue)
                .newStubbedResponse()
                .withStatus("500").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getUrl()).contains(fullQueryOne);
        assertThat(actualRequest.getUrl()).contains(fullQueryTwo);
        assertThat(actualRequest.getQuery()).containsEntry(expectedParamOne, expectedParamOneValue);
        assertThat(actualRequest.getQuery()).containsEntry(expectedParamTwo, expectedParamTwoValue);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithAuthorizationHeaderBasic() throws Exception {

        final String authorization = "bob:secret";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationBasic(authorization)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(authorization));

        assertThat(actualRequest.getHeaders()).containsEntry(BASIC.asYAMLProp(), encodedAuthorizationHeader);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithEmptyAuthorizationHeaderBasic() throws Exception {

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationBasic("")
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        final String encodedAuthorizationHeader = String.format("%s %s", "Basic", StringUtils.encodeBase64(""));

        assertThat(actualRequest.getHeaders()).containsEntry(BASIC.asYAMLProp(), encodedAuthorizationHeader);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithAuthorizationHeaderBearer() throws Exception {

        final String authorization = "Ym9iOnNlY3JldA==";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationBearer(authorization)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        final String authorizationHeader = String.format("%s %s", "Bearer", authorization);

        assertThat(actualRequest.getHeaders()).containsEntry(BEARER.asYAMLProp(), authorizationHeader);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithEmptyAuthorizationHeaderBearer() throws Exception {

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationBearer("")
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        final String authorizationHeader = String.format("%s %s", "Bearer", "");

        assertThat(actualRequest.getHeaders()).containsEntry(BEARER.asYAMLProp(), authorizationHeader);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithAuthorizationHeaderCustom() throws Exception {

        final String authorizationHeader = "CustomAuthorizationName AuthorizationValue";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationCustom(authorizationHeader)
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getHeaders()).containsEntry(CUSTOM.asYAMLProp(), authorizationHeader);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithEmptyAuthorizationHeaderCustom() throws Exception {

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withHeaderAuthorizationCustom("")
                .newStubbedResponse()
                .withStatus("301").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getHeaders()).containsEntry(CUSTOM.asYAMLProp(), "");
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WithMultipleHeaders() throws Exception {

        final String location = "/invoice/123";
        final String contentType = "application-json";

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .newStubbedResponse()
                .withHeaderContentType(contentType)
                .withHeaderLocation(location).build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubResponse actualResponse = actualHttpLifecycle.getResponse(true);

        assertThat(actualResponse.getHeaders()).containsEntry("location", location);
        assertThat(actualResponse.getHeaders()).containsEntry("content-type", contentType);
    }


    @Test
    public void shouldUnmarshall_WhenYAMLValid_WitQueryParamIsArrayHavingDoubleQuotes() throws Exception {

        final String expectedParamOne = "fruits";
        final String expectedParamOneValue = "[\"apple\",\"orange\",\"banana\"]";
        final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withQuery(expectedParamOne, String.format("'%s'", expectedParamOneValue))
                .newStubbedResponse()
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getUrl()).contains(fullQueryOne);
        assertThat(actualRequest.getQuery()).containsEntry(expectedParamOne, expectedParamOneValue);
    }

    @Test
    public void shouldUnmarshall_WhenYAMLValid_WitQueryParamIsArrayHavingSingleQuotes() throws Exception {

        final String expectedParamOne = "fruits";
        final String expectedParamOneValue = "['apple','orange','banana']";
        final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

        final String yaml = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri")
                .withQuery(expectedParamOne, String.format("\"%s\"", expectedParamOneValue))
                .newStubbedResponse()
                .withStatus("201").build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
        final StubRequest actualRequest = actualHttpLifecycle.getRequest();

        assertThat(actualRequest.getUrl()).contains(fullQueryOne);
        assertThat(actualRequest.getQuery()).containsEntry(expectedParamOne, expectedParamOneValue);
    }

    @Test
    public void shouldContainExpectedResourceIdHeaderUponSuccessfulYamlMarshall_WhenMultipleResponses() throws Exception {

        final String cycleOne = yamlBuilder
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final String cycleTwo = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(String.format("%s\n%s", cycleOne, cycleTwo)).getStubs();
        assertThat(loadedHttpCycles.size()).isEqualTo(2);

        for (int idx = 0; idx < loadedHttpCycles.size(); idx++) {
            final StubHttpLifecycle cycle = loadedHttpCycles.get(idx);
            final StubResponse cycleResponse = cycle.getResponse(true);

            assertThat(cycleResponse.getHeaders()).containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID);
            assertThat(cycleResponse.getHeaders().get(Common.HEADER_X_STUBBY_RESOURCE_ID)).isEqualTo(String.valueOf(idx));
        }
    }

    @Test
    public void shouldContainTheSameResourceIdHeader_ForEachSequencedResponse() throws Exception {

        final String yaml = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(yaml).getStubs();
        assertThat(loadedHttpCycles.size()).isEqualTo(1);

        final StubHttpLifecycle cycle = loadedHttpCycles.get(0);
        final List<StubResponse> allResponses = cycle.getResponses();

        for (int idx = 0; idx < allResponses.size(); idx++) {
            final StubResponse sequenceStubResponse = allResponses.get(idx);
            assertThat(sequenceStubResponse.getHeaders()).containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID);
            assertThat(sequenceStubResponse.getHeaders().get(Common.HEADER_X_STUBBY_RESOURCE_ID)).isEqualTo(String.valueOf(0));
        }
    }

    @Test
    public void shouldContainExpectedResourceIdHeaderUponSuccessfulYamlMarshall_WhenMultipleAndSqequencedResponses() throws Exception {

        final String cycleOne = yamlBuilder
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final String cycleTwo = yamlBuilder
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = yamlBuilder.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        final List<StubHttpLifecycle> loadedHttpCycles = unmarshall(String.format("%s%s%s%s%s", cycleOne, BR, cycleTwo, BR, cycleThree)).getStubs();
        assertThat(loadedHttpCycles.size()).isEqualTo(3);

        for (int resourceId = 0; resourceId < loadedHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = loadedHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(Common.HEADER_X_STUBBY_RESOURCE_ID);
                assertThat(sequenceStubResponse.getHeaders().get(Common.HEADER_X_STUBBY_RESOURCE_ID)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    @Test
    public void shouldUnmarshall_toCompleteYamlFromFile_WithIncludes() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/multi-include-main.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        final YamlParseResultSet yamlParseResultSet = new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));

        final StubHttpLifecycle actualHttpLifecycle = yamlParseResultSet.getStubs().get(0);
        assertThat(actualHttpLifecycle.getCompleteYaml()).isEqualTo(
                "- request:\n" +
                        "    method:\n" +
                        "    - GET\n" +
                        "    - POST\n" +
                        "    - PUT\n" +
                        "    url: ^/resources/asn/.*$\n" +
                        "  response:\n" +
                        "    status: 200\n" +
                        "    body: |\n" +
                        "      {\"status\": \"ASN found!\"}\n" +
                        "    headers:\n" +
                        "      content-type: application/json\n");

        final StubHttpLifecycle actualLastHttpLifecycle = yamlParseResultSet.getStubs().get(3);
        assertThat(actualLastHttpLifecycle.getCompleteYaml()).isEqualTo(
                "- request:\n" +
                        "    url: /individuals/.*/address$\n" +
                        "    method: PUT\n" +
                        "    post: |\n" +
                        "      {\"type\": \"HOME\"}\n" +
                        "  response:\n" +
                        "    body: OK\n" +
                        "    status: 200\n");

    }

    @Test
    public void shouldLoadYamlIncludesAsFileObjects() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/multi-include-main.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        final YamlParser yamlParser = new YamlParser();
        final List<File> yamlIncludes = yamlParser.getYamlIncludes(parentDirectory,
                yamlParser.loadRawYamlConfig(stubsConfigStream));

        assertThat(yamlIncludes.isEmpty()).isFalse();
        assertThat(yamlIncludes.size()).isEqualTo(3);
        assertThat(yamlIncludes.get(0).getAbsolutePath()).isEqualTo(parentDirectory + "/multi-included-service-1.yaml");
    }

    @Test
    public void shouldUnmarshall_toProxyConfigs() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/proxy-config-valid-config.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        final YamlParseResultSet yamlParseResultSet = new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
        final Map<String, StubProxyConfig> proxyConfigs = yamlParseResultSet.getProxyConfigs();
        assertThat(proxyConfigs.isEmpty()).isFalse();

        final StubProxyConfig defaultStubProxyConfig = proxyConfigs.get(StubProxyConfig.Builder.DEFAULT_UUID);
        assertThat(defaultStubProxyConfig.getUUID()).isEqualTo("default");
        assertThat(defaultStubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.AS_IS);
        assertThat(defaultStubProxyConfig.getProperties().size()).isEqualTo(1);
        assertThat(defaultStubProxyConfig.getProperties().get("endpoint")).isEqualTo("https://jsonplaceholder.typicode.com");

        final StubProxyConfig customStubProxyConfig = proxyConfigs.get("some-unique-name");
        assertThat(customStubProxyConfig.getUUID()).isEqualTo("some-unique-name");
        assertThat(customStubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.ADDITIVE);
        assertThat(customStubProxyConfig.getProperties().size()).isEqualTo(1);
        assertThat(customStubProxyConfig.getProperties().get("endpoint")).isEqualTo("https://jsonplaceholder.typicode.com");

        assertThat(defaultStubProxyConfig.getProxyConfigAsYAML()).isEqualTo(
                "- proxy-config:\n" +
                        "    description: this is a default catch-all config\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com\n" +
                        "    headers:\n" +
                        "      headerKeyOne: headerValueOne\n" +
                        "      headerKeyTwo: headerValueTwo\n");

        assertThat(customStubProxyConfig.getProxyConfigAsYAML()).isEqualTo(
                "- proxy-config:\n" +
                        "    description: woah! this is a unique proxy-config\n" +
                        "    uuid: some-unique-name\n" +
                        "    strategy: additive\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com\n");
    }

    @Test
    public void shouldUnmarshall_toProxyConfigsWithStubs() throws Exception {
        final URL yamlUrl = YamlParserTest.class.getResource("/yaml/proxy-config-valid-config-with-stubs.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();

        final YamlParseResultSet yamlParseResultSet = new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
        final Map<String, StubProxyConfig> proxyConfigs = yamlParseResultSet.getProxyConfigs();
        assertThat(proxyConfigs.isEmpty()).isFalse();

        final List<StubHttpLifecycle> stubs = yamlParseResultSet.getStubs();
        assertThat(stubs.isEmpty()).isFalse();
        assertThat(stubs.size()).isEqualTo(1);
        assertThat(stubs.get(0).getRequest().getUrl()).isEqualTo("^/resources/asn/.*$");
        assertThat(stubs.get(0).getResponses().get(0).getBody()).isEqualTo("{\"status\": \"ASN found!\"}");

        final StubProxyConfig defaultStubProxyConfig = proxyConfigs.get(StubProxyConfig.Builder.DEFAULT_UUID);
        assertThat(defaultStubProxyConfig.getUUID()).isEqualTo("default");
        assertThat(defaultStubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.AS_IS);
        assertThat(defaultStubProxyConfig.getProperties().size()).isEqualTo(1);
        assertThat(defaultStubProxyConfig.getProperties().get("endpoint")).isEqualTo("https://jsonplaceholder.typicode.com");

        final StubProxyConfig customStubProxyConfig = proxyConfigs.get("some-unique-name");
        assertThat(customStubProxyConfig.getUUID()).isEqualTo("some-unique-name");
        assertThat(customStubProxyConfig.getStrategy()).isEqualTo(StubProxyStrategy.ADDITIVE);
        assertThat(customStubProxyConfig.getProperties().size()).isEqualTo(1);
        assertThat(customStubProxyConfig.getProperties().get("endpoint")).isEqualTo("https://jsonplaceholder.typicode.com");

        assertThat(defaultStubProxyConfig.getProxyConfigAsYAML()).isEqualTo(
                "- proxy-config:\n" +
                        "    strategy: as-is\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com\n");

        assertThat(customStubProxyConfig.getProxyConfigAsYAML()).isEqualTo(
                "- proxy-config:\n" +
                        "    uuid: some-unique-name\n" +
                        "    strategy: additive\n" +
                        "    properties:\n" +
                        "      endpoint: https://jsonplaceholder.typicode.com\n");
    }

    @Test
    public void shouldThrowWhenProxyConfigWithInvalidStrategyName() throws Exception {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            final URL yamlUrl = YamlParserTest.class.getResource("/yaml/proxy-config-invalid-config.yaml");
            final InputStream stubsConfigStream = yamlUrl.openStream();
            final String parentDirectory = new File(yamlUrl.getPath()).getParent();

            new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
        });

        String expectedMessage = "invalid-strategy-name";
        String actualMessage = exception.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldThrowWhenProxyConfigWithDuplicateUUID() throws Exception {

        Exception exception = assertThrows(IOException.class, () -> {
            final URL yamlUrl = YamlParserTest.class.getResource("/yaml/proxy-config-duplicate-uuid.yaml");
            final InputStream stubsConfigStream = yamlUrl.openStream();
            final String parentDirectory = new File(yamlUrl.getPath()).getParent();

            new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream));
        });

        String expectedMessage = "Proxy config YAML contains duplicate UUIDs: some-unique-name";
        String actualMessage = exception.getMessage();

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    private YamlParseResultSet unmarshall(final String yaml) throws Exception {
        return new YamlParser().parse(".", yaml);
    }
}
