package io.github.azagniotov.stubby4j.stubs;


import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.tempFileFromString;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;

public class StubResponseBuilderTest {

    private StubResponse.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubResponse.Builder();
    }

    @Test
    public void shouldStage_WhenConfigurablePropertyAndFieldValuePresent() throws Exception {
        final String expectedFieldValue = "Hello!";
        final String orElse = "Boo!";
        final Optional<Object> fieldValueOptional = Optional.of(expectedFieldValue);

        builder.stage(BODY, fieldValueOptional);

        assertThat(builder.getStaged(String.class, BODY, orElse)).isEqualTo(expectedFieldValue);
    }

    @Test
    public void shouldNotStage_WhenConfigurablePropertyPresentButFieldValueMissing() throws Exception {
        final String orElse = "Boo!";
        final Optional<Object> fieldValueOptional = Optional.ofNullable(null);

        builder.stage(BODY, fieldValueOptional);

        assertThat(builder.getStaged(String.class, BODY, orElse)).isEqualTo(orElse);
    }

    @Test
    public void shouldReturnDefaultHttpStatusCode_WhenStatusFieldNull() throws Exception {
        assertThat(Code.OK).isEqualTo(builder.getHttpStatusCode());
    }

    @Test
    public void shouldReturnRespectiveHttpStatusCode_WhenStatusFieldSet() throws Exception {
        assertThat(Code.CREATED).isEqualTo(builder.withHttpStatusCode(Code.CREATED).getHttpStatusCode());
    }

    @Test
    public void shouldReturnBody_WhenFileIsNull() throws Exception {

        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody("this is some body")
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody("this is some body")
                .withFile(File.createTempFile("tmp", "tmp"))
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody("")
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws Exception {

        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody("")
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws Exception {

        final String expectedResponseBody = "content";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody("something")
                .withFile(tempFileFromString(expectedResponseBody))
                .build();

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttp() throws Exception {

        final String expectedResponseBody = "http://someurl.com";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(expectedResponseBody)
                .build();

        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttpUpperCase() throws Exception {

        final String expectedResponseBody = "HTtP://someurl.com";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(expectedResponseBody)
                .build();

        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyStartsWithHtt() throws Exception {

        final String expectedResponseBody = "htt://someurl.com";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(expectedResponseBody)
                .build();

        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp() throws Exception {

        final String expectedResponseBody = "some body content";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(expectedResponseBody)
                .build();

        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldFindBodyTokenized_WhenBodyContainsTemplateTokens() throws Exception {

        final String body = "some body with a <% token %>";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(body)
                .build();

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile() throws Exception {

        final String body = "some body";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(body)
                .withFile(tempFileFromString("file content with a <% token %>"))
                .build();

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile() throws Exception {

        final String body = "some body";
        final StubResponse stubResponse = builder
                .withHttpStatusCode(Code.OK)
                .withBody(body)
                .withFile(tempFileFromString("file content"))
                .build();

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isFalse();
    }
}
