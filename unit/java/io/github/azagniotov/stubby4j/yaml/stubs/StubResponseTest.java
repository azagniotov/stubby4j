package io.github.azagniotov.stubby4j.yaml.stubs;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.junit.Test;

import java.io.File;

import static io.github.azagniotov.stubby4j.utils.FileUtils.fileFromString;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/24/12, 10:49 AM
 */
public class StubResponseTest {

    @Test
    public void shouldReturnBody_WhenFileIsNull() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("200", "this is some body");

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnBody_WhenFileIsEmpty() throws Exception {

        final StubResponse stubResponse = new StubResponse("200", "this is some body", File.createTempFile("tmp", "tmp"), null, null);

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("this is some body").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenFileAndBodyAreNull() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("200", null);

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty() throws Exception {

        final StubResponse stubResponse = StubResponse.newStubResponse("200", "");

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnEmptyBody_WhenBodyIsEmpty_AndFileIsEmpty() throws Exception {

        final StubResponse stubResponse = new StubResponse("200", "", null, null, null);

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat("").isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldReturnFile_WhenFileNotEmpty_AndRegardlessOfBody() throws Exception {

        final String expectedResponseBody = "content";
        final StubResponse stubResponse = new StubResponse("200", "something", fileFromString(expectedResponseBody), null, null);

        final String actualResponseBody = StringUtils.newStringUtf8(stubResponse.getResponseBodyAsBytes());
        assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttp() throws Exception {

        final String expectedResponseBody = "http://someurl.com";
        final StubResponse stubResponse = new StubResponse("200", expectedResponseBody, null, null, null);

        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldRequireRecording_WhenBodyStartsWithHttpUpperCase() throws Exception {

        final String expectedResponseBody = "HTtP://someurl.com";
        final StubResponse stubResponse = new StubResponse("200", expectedResponseBody, null, null, null);

        assertThat(stubResponse.isRecordingRequired()).isTrue();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyStartsWithHtt() throws Exception {

        final String expectedResponseBody = "htt://someurl.com";
        final StubResponse stubResponse = new StubResponse("200", expectedResponseBody, null, null, null);

        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldNotRequireRecording_WhenBodyDoesnotStartWithHttp() throws Exception {

        final String expectedResponseBody = "some body content";
        final StubResponse stubResponse = new StubResponse("200", expectedResponseBody, null, null, null);

        assertThat(stubResponse.isRecordingRequired()).isFalse();
    }

    @Test
    public void shouldFindBodyTokenized_WhenBodyContainsTemplateTokens() throws Exception {

        final String body = "some body with a <% token %>";
        final StubResponse stubResponse = new StubResponse("200", body, null, null, null);

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileIsTemplateFile() throws Exception {

        final String body = "some body";
        final StubResponse stubResponse = new StubResponse("200", body, fileFromString("file content with a <% token %>"), null, null);

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isTrue();
    }

    @Test
    public void shouldFindBodyNotTokenized_WhenRawFileNotTemplateFile() throws Exception {

        final String body = "some body";
        final StubResponse stubResponse = new StubResponse("200", body, fileFromString("file content"), null, null);

        assertThat(stubResponse.isBodyContainsTemplateTokens()).isFalse();
    }
}
