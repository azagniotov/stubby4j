package io.github.azagniotov.stubby4j.stubs;

import io.github.azagniotov.stubby4j.utils.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BASIC;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.BEARER;
import static io.github.azagniotov.stubby4j.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.yaml.ConfigurableYAMLProperty.BODY;


@RunWith(MockitoJUnitRunner.class)
public class StubRequestBuilderTest {

    private StubRequest.Builder builder;

    @Before
    public void setUp() throws Exception {
        builder = new StubRequest.Builder();
    }

    @After
    public void cleanup() throws Exception {
        RegexParser.REGEX_PATTERN_CACHE.clear();
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
    public void stubbedRequestEqualsAssertingRequest_WhenNullUrlStubbed_AndNullUrlSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl(null).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(null).withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenEmptyUrlStubbed_AndEmptyUrlSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenNullUrlStubbed_ButUrlSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl(null).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlStubbed_ButNullUrlSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(null).withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlStubbed_ButNoUrlSubmitted() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlNotStubbed_ButUrlSubmitted() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest = builder.withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(url).withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenRootUrlsEquals() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedRootUrlStartsWithRegex_ButSubmittedUrlRoot() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("^/$").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlHasOptionalTrailingSlash_ButNoSlashSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123/?").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlHasOptionalTrailingSlash_ButSlashSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123/?").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenStubbedUrlHasRequiredTrailingSlash_ButNoSlashSubmitted() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123/").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexExact_AndUrlsEqual() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("^/invoice/123$").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexBeginsWith() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("^/invoice/123.*").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/12345").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenSubmittedUrlLonger() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/12345").withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenStubbedUrlRegexBeginsWith_AndSubmittedUrlWrong() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("^/invoice/123").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/1").withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenSubmittedUrlShorter() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/1").withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlsEquals() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/invoice/123").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexEndsWith_AndSubmittedUrlHasExtraBeggining() throws Exception {

        final StubRequest expectedRequest = builder.withUrl(".*/invoice/123$").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/some/beggining/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexAnythingAround_AndUrlsEqual() throws Exception {

        final StubRequest expectedRequest = builder.withUrl(".*/invoice/123.*").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexGroups_AndUrlsEqual() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("(.*)(/invoice/123)(.*)").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/invoice/123").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentUri() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl("/invoice/788")
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramTwo, paramTwoValue)
                        .withQuery(paramOne, paramOneValue).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenMethodStubbed_ButLowerCasedMethodSubmitted() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(url).withMethod("get").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenMethodStubbed_ButNoMethodSubmitted() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(url).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenNoMethodStubbed_ButMethodSubmitted() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest = builder.withUrl(url).build();
        final StubRequest assertingRequest = builder.withUrl(url).withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void shouldAddMethod_WhenGivenMethodArgumentSet() throws Exception {

        final StubRequest expectedRequest = builder
                .withMethod("GET")
                .withMethod("POST")
                .withMethod("HEAD").build();

        assertThat(expectedRequest.getMethod().size()).isEqualTo(3);
        assertThat(expectedRequest.getMethod()).containsExactly("GET", "POST", "HEAD");
    }

    @Test
    public void shouldAddMethod_WhenGivenMethodArgumentEmpty() throws Exception {

        final StubRequest expectedRequest = builder
                .withMethod("GET")
                .withMethod("")
                .withMethod("HEAD").build();

        assertThat(expectedRequest.getMethod().size()).isEqualTo(2);
        assertThat(expectedRequest.getMethod()).containsExactly("GET", "HEAD");
    }

    @Test
    public void shouldAddMethod_WhenGivenMethodArgumentNull() throws Exception {

        final StubRequest expectedRequest = builder
                .withMethod("GET")
                .withMethod(null)
                .withMethod("HEAD").build();

        assertThat(expectedRequest.getMethod().size()).isEqualTo(2);
        assertThat(expectedRequest.getMethod()).containsExactly("GET", "HEAD");
    }

    @Test
    public void shouldGetPostBody_WhenPostProvided_ButFileIsNull() throws Exception {

        final String url = "/invoice/789";

        final String postBody = "Hello";
        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withPost(postBody)
                        .withMethodPost().build();

        assertThat(expectedRequest.getPostBody()).isEqualTo(postBody);
    }

    @Test
    public void shouldGetPostBody_WhenPostProvided_ButFileIsEmpty() throws Exception {

        final String url = "/invoice/789";

        final String postBody = "Hello";
        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withPost(postBody)
                        .withFile(FileUtils.tempFileFromString(""))
                        .withMethodPost().build();

        assertThat(expectedRequest.getPostBody()).isEqualTo(postBody);
    }

    @Test
    public void shouldGetPostBody_WhenPostNotProvided_ButFileSet() throws Exception {

        final String url = "/invoice/789";

        final String fileContent = "Hello World!";
        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withFile(FileUtils.tempFileFromString(fileContent))
                        .withMethodPost().build();

        assertThat(expectedRequest.getPostBody()).isEqualTo(fileContent);
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentHttpMethod() throws Exception {

        final String url = "/invoice/789";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl(url).withMethodPost().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentPostBody() throws Exception {

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost("some post").build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost("different post").build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenPostBodyWasStubbed_ButNoPostBodySubmitted() throws Exception {

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost("some stubbed post").build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenNoPostBodyWasStubbed_ButPostBodyWasSubmitted() throws Exception {

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost().build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost("some post").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void isSecured_WhenAuthorizationBasicStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(BASIC.asYAMLProp(), "123").build();

        assertThat(stubRequest.isSecured()).isTrue();
    }

    @Test
    public void isSecured_WhenAuthorizationBearerStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(BEARER.asYAMLProp(), "123").build();

        assertThat(stubRequest.isSecured()).isTrue();
    }

    @Test
    public void isSecured_WhenAuthorizationCustomStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(CUSTOM.asYAMLProp(), "Custom 123").build();

        assertThat(stubRequest.isSecured()).isTrue();
    }

    @Test
    public void isNotSecured_WhenNoAuthorizationStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet().build();

        assertThat(stubRequest.isSecured()).isFalse();
    }

    @Test
    public void shouldGetAuthorizationTypeBasic_WhenBasicAuthorizationStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(BASIC.asYAMLProp(), "123").build();

        assertThat(stubRequest.getStubbedAuthorizationType()).isEqualTo(BASIC);
    }

    @Test
    public void shouldGetAuthorizationTypeBearer_WhenBearerAuthorizationStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(BEARER.asYAMLProp(), "123").build();

        assertThat(stubRequest.getStubbedAuthorizationType()).isEqualTo(BEARER);
    }

    @Test
    public void shouldGetAuthorizationTypeCustom_WhenCustomAuthorizationStubbed() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withHeader(CUSTOM.asYAMLProp(), "Custom 123").build();

        assertThat(stubRequest.getStubbedAuthorizationType()).isEqualTo(CUSTOM);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllHttpHeadersMatch() throws Exception {

        final String url = "/invoice/123";
        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenHeadersWereStubbed_ButNoHeadersSetToAssert() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength("30")
                        .withHeaderContentLanguage("en-US").build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenNoHeadersWereStubbed_ButHeadersWereSetToAssert() throws Exception {

        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet().build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength("30")
                        .withHeaderContentLanguage("en-US").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllHeadersSubmittedCamelCased() throws Exception {

        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeader("Content-Length", contentLength)
                        .withHeader("Content-Language", contentLanguage).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllHeadersStubbedCamelCased() throws Exception {

        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeader("Content-Length", contentLength)
                        .withHeader("Content-Language", contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();


        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenSomeHeadersMismatches() throws Exception {

        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withHeaderContentType("application/xml")
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationJsonContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenNotAllHeadersSetToAssert() throws Exception {

        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllStubbedHeadersMatch() throws Exception {

        final String contentLength = "30";
        final String contentLanguage = "en-US";
        final String url = "/invoice/123";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage)
                        .withHeaderContentEncoding("UTF-8")
                        .withHeaderPragma("no-cache").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenNoQueryParamsWereStubbed_ButQueryParamsWereSetToAssert() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead().build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamsWereStubbed_ButNoQueryParamsWereSetToAssert() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllQueryParamsMatch() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenAllStubbedQueryParamsMatch() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withQuery("paramThree", "three").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenNotAllQueryParamsSetToAssert() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withQuery("paramThree", "three").build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamsMismatch() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, "three").build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamsInDifferentOrder() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "two";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramTwo, paramTwoValue)
                        .withQuery(paramOne, paramOneValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamIsArray() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[\"cheburashka\",\"wendy\"]";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenThereLargeSetupOfStubbedProperties() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[%22alex%22,%22wendy%22]";

        final String contentType = "application/blah";
        final String contentLength = "30";
        final String contentLanguage = "en-US";

        final String url = "/invoice/123";
        final String postBody = "this is a post body";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withHeaderContentType(contentType)
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withHeaderContentType(contentType)
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage)
                        .withHeaderContentEncoding("UTF-8")
                        .withHeaderPragma("no-cache").build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenQueryParamRegexIsMatching() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "session_id";
        final String paramTwoRegex = "^user_\\d{32}_(local|remote)";
        final String paramTwoAssertingValue = "user_29898678635097503927398653027523_remote";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamRegexDoesNotMatch() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "session_id";
        final String paramTwoRegex = "^user_\\d{32}_local";
        final String paramTwoAssertingValue = "user_12345_local";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoAssertingValue).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenHeaderRegexIsMatching() throws Exception {

        final String headerOne = "headerOne";
        final String headerOneValue = "one";

        final String headerTwo = "headerTwo";
        final String headerTwoRegex = "^[a-z]{4}_\\d{32}_(local|remote)";
        final String headerTwoAssertingValue = "user_29898678635097503927398653027523_remote";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withHeader(headerOne, headerOneValue)
                        .withHeader(headerTwo, headerTwoRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withHeader(headerOne, headerOneValue)
                        .withHeader(headerTwo, headerTwoAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenHeaderRegexDoesNotMatch() throws Exception {

        final String headerOne = "headerOne";
        final String headerOneValue = "one";

        final String headerTwo = "headerTwo";
        final String headerTwoRegex = "^[a-z]{4}_\\d{32}_(local|remote)";
        final String headerTwoAssertingValue = "usEr_29898678635097503927398653027523_remote";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withHeader(headerOne, headerOneValue)
                        .withHeader(headerTwo, headerTwoRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodHead()
                        .withHeader(headerOne, headerOneValue)
                        .withHeader(headerTwo, headerTwoAssertingValue).build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingAnyPostWithoutNewLineCharacter() throws Exception {

        final String postRegex = ".*";

        final String postAssertingValue =
                "Here's the story of a lovely lady, " +
                        "Who was bringing up three very lovely girls. " +
                        "All of them had hair of gold, like their mother, " +
                        "The youngest one in curls. " +
                        "Here's the story, of a man named Brady, " +
                        "Who was busy with three boys of his own. " +
                        "They were four men, living all together, " +
                        "Yet they were all alone.";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithLinefeedChar() throws Exception {

        final String postRegex = ".*";

        final String postAssertingValue =
                "Here's the story of a lovely lady,\n" +
                        "Who was bringing up three very lovely girls.\n" +
                        "All of them had hair of gold, like their mother,\n" +
                        "The youngest one in curls.\n" +
                        "Here's the story, of a man named Brady,\n" +
                        "Who was busy with three boys of his own.\n" +
                        "They were four men, living all together,\n" +
                        "Yet they were all alone.";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithSystemLineChar() throws Exception {

        final String postRegex = ".*";

        final String postAssertingValue =
                "Here's the story of a lovely lady," + BR +
                        "Who was bringing up three very lovely girls." + BR +
                        "All of them had hair of gold, like their mother," + BR +
                        "The youngest one in curls." + BR +
                        "Here's the story, of a man named Brady," + BR +
                        "Who was busy with three boys of his own." + BR +
                        "They were four men, living all together," + BR +
                        "Yet they were all alone.";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithCarriageReturnChar() throws Exception {

        final String postRegex = ".*";

        final String postAssertingValue =
                "Here's the story of a lovely lady,\r" +
                        "Who was bringing up three very lovely girls.\r" +
                        "All of them had hair of gold, like their mother,\r" +
                        "The youngest one in curls.\r" +
                        "Here's the story, of a man named Brady,\r" +
                        "Who was busy with three boys of his own.\r" +
                        "They were four men, living all together,\r" +
                        "Yet they were all alone.";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithCarriageReturnLinefeedChars() throws Exception {

        final String postRegex = ".*";

        final String postAssertingValue =
                "Here's the story of a lovely lady,\r\n" +
                        "Who was bringing up three very lovely girls.\r\n" +
                        "All of them had hair of gold, like their mother,\r\n" +
                        "The youngest one in curls.\r\n" +
                        "Here's the story, of a man named Brady,\r\n" +
                        "Who was busy with three boys of his own.\r\n" +
                        "They were four men, living all together,\r\n" +
                        "Yet they were all alone.";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPlainPostRegexStubbedAndPlainTextPosted() throws Exception {

        final String postRegex = "This is a text with (.*) the end of summer!";
        final String postAssertingValue = "This is a text with DANCING IN THE RAIN, the end of summer!";
        final String url = "/post";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenJsonPostRegexStubbedAndJsonPosted() throws Exception {

        final String postRegex = "{\"userId\":\"19\",\"requestId\":\"(.*)\",\"transactionDate\":\"(.*)\",\"transactionTime\":\"(.*)\"}";
        final String postAssertingValue = "{\"userId\":\"19\",\"requestId\":\"12345\",\"transactionDate\":\"98765\",\"transactionTime\":\"11111\"}";
        final String url = "/post";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withApplicationJsonContentType()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withApplicationJsonContentType()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);

        final Map<String, String> regexGroups = assertingRequest.getRegexGroups();
        assertThat(regexGroups.get("post.0")).isEqualTo(postAssertingValue);
        assertThat(regexGroups.get("post.1")).isEqualTo("12345");
        assertThat(regexGroups.get("post.2")).isEqualTo("98765");
        assertThat(regexGroups.get("post.3")).isEqualTo("11111");
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenComplexJsonPostRegexStubbedAndJsonPosted() throws Exception {

        final String postRegex = "{\"objects\": [{\"key\": \"value\"}, {\"key\": \"value\"}, {\"key\": {\"key\": \"(.*)\"}}]}";
        final String postAssertingValue = "{\"objects\": [{\"key\": \"value\"}, {\"key\": \"value\"}, {\"key\": {\"key\": \"12345\"}}]}";
        final String url = "/post";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withApplicationJsonContentType()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withApplicationJsonContentType()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);

        final Map<String, String> regexGroups = assertingRequest.getRegexGroups();
        assertThat(regexGroups.get("post.0")).isEqualTo(postAssertingValue);
        assertThat(regexGroups.get("post.1")).isEqualTo("12345");
    }

    @Test
    public void shouldComputeRegexPatterns() throws Exception {

        final String url = "^/resources/asn/.*$";
        final String post = "{\"objects\": [{\"key\": \"value\"}, {\"key\": \"value\"}, {\"key\": {\"key\": \"(.*)\"}}]}";

        final StubRequest stubRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withApplicationJsonContentType()
                        .withPost(post).build();
        stubRequest.compileRegexPatternsAndCache();

        assertThat(RegexParser.REGEX_PATTERN_CACHE.size().get()).isEqualTo(3);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingSubsectionOfMultiLineJsonPost() throws Exception {

        final String postRegex = ".*(\"id\": \"123\").*";

        final String postAssertingValue =
                "{" +
                        "   \"products\": [" +
                        "      {" +
                        "      \"id\": \"123\"," +
                        "      }," +
                        "      {" +
                        "      \"id\": \"789\"," +
                        "      }" +
                        "   ]" +
                        "}";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postAssertingValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingSingleLinePost() throws Exception {

        final String postRegex = "^This is an invoice: \\d{3} from today";
        final String postValue = "This is an invoice: 889 from today";

        final String url = "/invoice/789";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postRegex).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withMethodHead()
                        .withPost(postValue).build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenThereLargeSetupOfStubbedProperties_ButNotAllHeadersSetToAssert() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[%22alex%22,%22wendy%22]";

        final String contentLength = "30";
        final String contentLanguage = "en-US";

        final String url = "/invoice/123";
        final String postBody = "this is a post body";

        final StubRequest expectedRequest =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest assertingRequest =
                builder.withUrl(url)
                        .withMethodPost()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength("888")
                        .withHeaderContentLanguage(contentLanguage)
                        .withHeaderContentEncoding("UTF-8")
                        .withHeaderPragma("no-cache").build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenRegexifiedUrlDoesNotBeginWithRegexSign_ItsNotProcessedAsRegex() throws Exception {

        final String url = ".*account.*";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/some/products/account/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenRegexifiedUrlBeginsWithRegexSign_ItsProcessedAsRegex() throws Exception {

        final String url = "^.*account.*";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/some/products/account/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlPartiallyRegexified_ButGoodAssertionUrlConfigured() throws Exception {

        final String url = "^/products/[0-9]+/?$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/products/12345/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlRegexified_ButGoodAssertionUrlConfigured() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/abc/12345/").withMethodGet().build();

        assertThat(assertingRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlConditionallyRegexified_ButGoodAssertionUrlConfigured() throws Exception {

        final String url = "^/(cats|dogs)/?(.*)";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder.withUrl("/cats/blah/again/").withMethodGet().build());
            add(builder.withUrl("/cats/blah/").withMethodGet().build());
            add(builder.withUrl("/dogs/blah/").withMethodGet().build());
            add(builder.withUrl("/dogs/").withMethodGet().build());
            add(builder.withUrl("/dogs").withMethodGet().build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlConditionallyRegexified_ButGoodAssertionUrlConfigured_v2() throws Exception {

        final String url = "^/(account|profile)/user/session/[a-zA-Z0-9]{32}/?";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder.withUrl("/account/user/session/d41d8cd98f00b204e9800998ecf8427e").withMethodGet().build());
            add(builder.withUrl("/account/user/session/d41d8cd98f00b204e9800998ecf8427e/").withMethodGet().build());
            add(builder.withUrl("/profile/user/session/d41d8cd98f00b204e9800998ecf8427e").withMethodGet().build());
            add(builder.withUrl("/profile/user/session/d41d8cd98f00b204e9800998ecf8427e/").withMethodGet().build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isEqualTo(expectedRequest);
        }
    }


    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexified_ButBadAssertionUrlConfigured() throws Exception {

        final String url = "^/[a-z]{3}/[0-9]+/?$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/abcm/12345/").withMethodGet().build();

        assertThat(assertingRequest).isNotEqualTo(expectedRequest);
    }


    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenComplexUrlRegexified_ButGoodAssertionUrlConfigured() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withMethodGet().build());
            add(builder.withUrl("/abc-efg/12/KM/23423").withMethodGet().build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerty").withMethodGet().build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenComplexUrlRegexified_ButBadAssertionUrlConfigured() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder.withUrl("/abca-efg/12/KM/jhgjkhg234234l2").withMethodGet().build());
            add(builder.withUrl("/abcefg/12/KM/23423").withMethodGet().build());
            add(builder.withUrl("/aaa-aaa/00/Af/qwerty").withMethodGet().build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerTy").withMethodGet().build());
            add(builder.withUrl("/aaa-aaa/009/AA/qwerty").withMethodGet().build());
            add(builder.withUrl("/AAA-AAA/00/AA/qwerty").withMethodGet().build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isNotEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestShouldReturnEmptyRegexGroup_WhenValidRegexHasNoMatcherGroups() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().size()).isEqualTo(1);
        assertThat(assertingRequest.getRegexGroups().toString()).isEqualTo("{url.0=/abc-efg/12/KM/jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnOneRegexGroup_WhenValidRegexHasMatcherGroups() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/([a-z0-9]+)$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(2);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(2);
        assertThat(assertingRequest.getRegexGroups().toString()).isEqualTo("{url.0=/abc-efg/12/KM/jhgjkhg234234l2, url.1=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenValidRegexHasMatcherGroups() throws Exception {

        final String url = "^/([a-z]{3}-[a-z]{3})/[0-9]{2}/[A-Z]{2}/([a-z0-9]+)$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(3);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(3);
        assertThat(assertingRequest.getRegexGroups().toString()).isEqualTo("{url.0=/abc-efg/12/KM/jhgjkhg234234l2, url.1=abc-efg, url.2=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenRegexHasCapturingGroupWhichIsAlsoFullRegex() throws Exception {

        final String url = "^([a-z]{3})$";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("abc").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(2);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(2);
        assertThat(assertingRequest.getRegexGroups().toString()).isEqualTo("{url.0=abc, url.1=abc}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenValidRegexHasCapturingGroupsInMultipleProperties() throws Exception {

        final String url = "^/([a-z]{3}-[a-z]{3})/[0-9]{2}/[A-Z]{2}/([a-z0-9]+)$";

        final StubRequest expectedRequest =
                builder.withUrl(url).withMethodGet().withQuery("paramOne", "(\\d{1,})").build();
        final StubRequest assertingRequest =
                builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withQuery("paramOne", "12345").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(5);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(5);
        assertThat(assertingRequest.getRegexGroups()
                .toString()).isEqualTo("{query.paramOne.0=12345, query.paramOne.1=12345, url.0=/abc-efg/12/KM/jhgjkhg234234l2, url.1=abc-efg, url.2=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenValidRegexHasCapturingGroupsInQuery() throws Exception {

        final String url = "^/([a-z]{3}-[a-z]{3})/[0-9]{2}/[A-Z]{2}/([a-z0-9]+)$";

        final StubRequest expectedRequest =
                builder.withUrl(url).withMethodGet().withQuery("paramOne", "(\\d{1,})").withQuery("paramTwo", "([A-Z]{5})").build();
        final StubRequest assertingRequest =
                builder.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withQuery("paramOne", "12345").withQuery("paramTwo", "ABCDE").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(7);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(7);
        assertThat(assertingRequest.getRegexGroups()
                .toString()).isEqualTo("{query.paramOne.0=12345, query.paramOne.1=12345, query.paramTwo.0=ABCDE, query.paramTwo.1=ABCDE, url.0=/abc-efg/12/KM/jhgjkhg234234l2, url.1=abc-efg, url.2=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenValidRegexHasOneSubCapturingGroup() throws Exception {

        final String url = "^/([a-z]{3}-([a-z]{3}))/([a-z0-9]+)$";

        final StubRequest expectedRequest =
                builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest =
                builder.withUrl("/abc-efg/jhgjkhg234234l2").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(4);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(4);
        assertThat(assertingRequest.getRegexGroups()
                .toString()).isEqualTo("{url.0=/abc-efg/jhgjkhg234234l2, url.1=abc-efg, url.2=efg, url.3=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestShouldReturnMultipleRegexGroups_WhenValidRegexHasMultipleSubCapturingGroups() throws Exception {

        final String url = "^/(([a-z]{3})-([a-z]{3}))/([a-z0-9]+)$";

        final StubRequest expectedRequest =
                builder.withUrl(url).withMethodGet().build();
        final StubRequest assertingRequest =
                builder.withUrl("/abc-efg/jhgjkhg234234l2").withMethodGet().build();

        final boolean equals = assertingRequest.equals(expectedRequest);
        assertThat(equals).isTrue();
        assertThat(assertingRequest.getRegexGroups().keySet().size()).isEqualTo(5);
        assertThat(assertingRequest.getRegexGroups().values().size()).isEqualTo(5);
        assertThat(assertingRequest.getRegexGroups()
                .toString()).isEqualTo("{url.0=/abc-efg/jhgjkhg234234l2, url.1=abc-efg, url.2=abc, url.3=efg, url.4=jhgjkhg234234l2}");
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenUrlRegexifiedDoesNotAccommodateForQueryString() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder
                    .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/abc-efg/12/KM/23423")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerty")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedHasQueryString() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{8}&paramTwo=[a-zA-Z]{8}";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder
                    .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
                    .withMethodGet()
                    .withQuery("paramOne", "wqePwrew")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/abc-efg/12/KM/23423")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerty")
                    .withMethodGet()
                    .withQuery("paramOne", "aaaaaaaa")
                    .withQuery("paramTwo", "QwErTyUi").build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isNotEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestEqualsAssertingRequest_WhenStaticUrlHasRegexifiedQueryString() throws Exception {

        final StubRequest expectedRequest = builder.withUrl("/atom/feed")
                .withMethodGet()
                .withQuery("min-results", "\\d+")
                .withQuery("max-results", "\\d+").build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder
                    .withUrl("/atom/feed")
                    .withMethodGet()
                    .withQuery("min-results", "0")
                    .withQuery("max-results", "0").build());
            add(builder.withUrl("/atom/feed")
                    .withMethodGet()
                    .withQuery("min-results", "1")
                    .withQuery("max-results", "5").build());
            add(builder.withUrl("/atom/feed")
                    .withMethodGet()
                    .withQuery("min-results", "4654645756756")
                    .withQuery("max-results", "5675675686786786785675464564564").build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedWithStaticQueryString() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=valueOne&paramTwo=valueTwo";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder
                    .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/abc-efg/12/KM/23423")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerty")
                    .withMethodGet()
                    .withQuery("paramOne", "valueOne")
                    .withQuery("paramTwo", "valueTwo").build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isNotEqualTo(expectedRequest);
        }
    }

    @Test
    public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedAccomodatesForQueryString_ButBadAssertionUrlConfigured() throws Exception {

        final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{8}&paramTwo=[a-zA-Z]{8}";

        final StubRequest expectedRequest = builder.withUrl(url).withMethodGet().build();

        final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
            add(builder
                    .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
                    .withMethodGet()
                    .withQuery("paramSix", "wqePwrew")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/abc-efg/12/KM/23423")
                    .withMethodGet()
                    .withQuery("paramOne", "12345678")
                    .withQuery("paramTwo", "valueTwo").build());
            add(builder.withUrl("/aaa-aaa/00/AA/qwerty")
                    .withMethodGet()
                    .withQuery("paramOne", "aaa7aaaa")
                    .withQuery("paramTwo", "QwErTyUi").build());
        }};

        for (final StubRequest assertingRequest : assertingRequests) {
            assertThat(assertingRequest).isNotEqualTo(expectedRequest);
        }
    }

    @Test
    public void shouldfindStubRequestNotEqual_WhenComparedToNull() throws Exception {
        final StubRequest expectedRequest = builder.withUrl("/products/12345/").withMethodGet().build();

        assertThat(expectedRequest).isNotEqualTo(null);
    }

    @Test
    public void shouldfindStubRequestNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
        final StubRequest expectedRequest = builder.withUrl("/products/12345/").withMethodGet().build();
        final Object assertingObject = StubResponse.okResponse();

        final boolean assertionResult = expectedRequest.equals(assertingObject);
        assertThat(assertionResult).isFalse();
    }

    @Test
    public void shouldfindStubRequestEqual_WhenComparedToSameInstanceClass() throws Exception {
        final StubRequest expectedRequest = builder.withUrl("/products/12345/").withMethodGet().build();
        final Object assertingObject = builder.build();

        final boolean assertionResult = assertingObject.equals(expectedRequest);
        assertThat(assertionResult).isFalse();
    }

    @Test
    public void shouldfindStubRequestEqual_WhenComparedToDifferentObjectWithSameProperties() throws Exception {
        final StubRequest expectedRequest = builder.withUrl("/products/12345/").withMethodGet().build();
        final StubRequest assertingRequest = builder.withUrl("/products/12345/").withMethodGet().build();

        final boolean assertionResultOne = assertingRequest.equals(expectedRequest);
        final boolean assertionResultTwo = expectedRequest.equals(assertingRequest);

        assertThat(assertionResultOne).isTrue();
        assertThat(assertionResultTwo).isTrue();
    }

    @Test
    public void shouldfindStubRequestEqual_WhenComparedToSameIdentity() throws Exception {
        final StubRequest expectedRequest = builder.withUrl("/products/12345/").withMethodGet().build();

        assertThat(expectedRequest).isEqualTo(expectedRequest);
    }


    @Test
    public void shouldFindTwoHashCodesEqual_WhenTwoRequestAreTheSame() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[%22alex%22,%22wendy%22]";

        final String contentLength = "30";
        final String contentLanguage = "en-US";

        final String url = "/invoice/123";
        final String postBody = "this is a post body";

        final StubRequest requestOne =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withFile(FileUtils.tempFileFromString("bytes"))
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest requestTwo =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withFile(FileUtils.tempFileFromString("bytes"))
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
    }

    @Test
    public void shouldNotFindTwoHashCodesEqual_WhenTwoRequestHaveDifferentAmdNullPostBody() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[%22alex%22,%22wendy%22]";

        final String contentLength = "30";
        final String contentLanguage = "en-US";

        final String url = "/invoice/123";
        final String postBody = "this is a post body";

        final StubRequest requestOne =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(null)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest requestTwo =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(requestOne.hashCode()).isNotEqualTo(requestTwo.hashCode());
    }

    @Test
    public void shouldNotFindTwoHashCodesEqual_WhenTwoRequestHaveDifferentHeaderValue() throws Exception {

        final String paramOne = "paramOne";
        final String paramOneValue = "one";

        final String paramTwo = "paramTwo";
        final String paramTwoValue = "[%22alex%22,%22wendy%22]";

        final String contentLength = "30";
        final String contentLanguage = "en-US";

        final String url = "/invoice/123";
        final String postBody = "this is a post body";

        final StubRequest requestOne =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength(contentLength)
                        .withHeaderContentLanguage(contentLanguage).build();

        final StubRequest requestTwo =
                builder.withUrl(url)
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(postBody)
                        .withQuery(paramOne, paramOneValue)
                        .withQuery(paramTwo, paramTwoValue)
                        .withApplicationXmlContentType()
                        .withHeaderContentLength("31")
                        .withHeaderContentLanguage(contentLanguage).build();

        assertThat(requestOne.hashCode()).isNotEqualTo(requestTwo.hashCode());
    }

    @Test
    public void shouldFindTwoHashCodesEqual_WhenTwoRequestHaveMethodAndUrlNull() throws Exception {

        final StubRequest requestOne =
                builder.withUrl(null)
                        .withMethod(null).build();

        final StubRequest requestTwo =
                builder.withUrl(null)
                        .withMethod(null).build();

        assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
    }

    @Test
    public void shouldFindTwoHashCodesEqual_WhenTwoRequestHaveUrlNull() throws Exception {

        final StubRequest requestOne =
                builder.withUrl(null)
                        .withMethodGet().build();

        final StubRequest requestTwo =
                builder.withUrl(null)
                        .withMethodGet().build();

        assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
    }

    @Test
    public void shouldMatchExpectedToStringOutput_WhenActualRequestHasTheSameOutput() throws Exception {

        final StubRequest actualRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost("this is a post body")
                        .withQuery("paramOne", "paramOneValue")
                        .withQuery("paramTwo", "paramTwoValue")
                        .withHeader("headerThree", "headerThreeValue")
                        .withHeader("headerTwo", "headerTwoValue")
                        .withHeader("headerOne", "headerOneValue").build();


        final String expectedToStringOutput = "StubRequest{" +
                "url=/invoice/123, " +
                "method=[GET, POST, PUT], " +
                "post=this is a post body, " +
                "query={paramOne=paramOneValue, paramTwo=paramTwoValue}, " +
                "headers={headerthree=headerThreeValue, headertwo=headerTwoValue, headerone=headerOneValue}}";

        assertThat(actualRequest.toString()).isEqualTo(expectedToStringOutput);
    }

    @Test
    public void shouldMatchExpectedToStringOutput_WhenActualRequestHasNullBody() throws Exception {

        final StubRequest actualRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(null)
                        .withQuery("paramOne", "paramOneValue")
                        .withQuery("paramTwo", "paramTwoValue")
                        .withHeader("headerThree", "headerThreeValue")
                        .withHeader("headerTwo", "headerTwoValue")
                        .withHeader("headerOne", "headerOneValue").build();

        final String expectedToStringOutput = "StubRequest{" +
                "url=/invoice/123, " +
                "method=[GET, POST, PUT], " +
                "query={paramOne=paramOneValue, paramTwo=paramTwoValue}, " +
                "headers={headerthree=headerThreeValue, headertwo=headerTwoValue, headerone=headerOneValue}}";

        assertThat(actualRequest.toString()).isEqualTo(expectedToStringOutput);
    }

    @Test
    public void shouldMatchExpectedToStringOutput_WhenActualRequestHasNullHeaderValue() throws Exception {

        final StubRequest actualRequest =
                builder.withUrl("/invoice/123")
                        .withMethodGet()
                        .withMethodPost()
                        .withMethodPut()
                        .withPost(null)
                        .withQuery("paramOne", "paramOneValue")
                        .withQuery("paramTwo", "paramTwoValue")
                        .withHeader("headerThree", "headerThreeValue")
                        .withHeader("headerTwo", "headerTwoValue")
                        .withHeader("headerOne", null).build();

        final String expectedToStringOutput = "StubRequest{" +
                "url=/invoice/123, " +
                "method=[GET, POST, PUT], " +
                "query={paramOne=paramOneValue, paramTwo=paramTwoValue}, " +
                "headers={headerthree=headerThreeValue, headertwo=headerTwoValue, headerone=null}}";

        assertThat(actualRequest.toString()).isEqualTo(expectedToStringOutput);
    }

    @Test
    public void shouldMatchExpectedToStringOutput_WhenActualRequestHasAllNullFields() throws Exception {

        final StubRequest actualRequest =
                builder.withUrl(null)
                        .withMethod(null)
                        .withPost(null).build();


        final String expectedToStringOutput = "StubRequest{" +
                "url=null, " +
                "method=[], " +
                "query={}, " +
                "headers={}}";

        assertThat(actualRequest.toString()).isEqualTo(expectedToStringOutput);
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostNullAndMethodGet() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("GET")
                        .withPost(null).build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostStubbedAndMethodGet() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("GET")
                        .withPost("stubbed").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostNullAndMethodPut() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PUT")
                        .withPost(null).build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostEmptyAndMethodPut() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PUT")
                        .withPost("").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindRequestBodyStubbed_WhenPostStubbedAndMethodPut() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PUT")
                        .withPost("stubbed").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isTrue();
    }

    @Test
    public void shouldFindRequestBodyStubbed_WhenPostStubbedAndMethodPatch() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PATCH")
                        .withPost("stubbed").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isTrue();
    }

    @Test
    public void shouldFindRequestBodyStubbed_WhenFileStubbedAndMethodPatch() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PATCH")
                        .withFile(FileUtils.tempFileFromString("hello")).build();

        assertThat(stubRequest.isRequestBodyStubbed()).isTrue();
    }

    @Test
    public void shouldFindRequestBodyNotStubbed_WhenJustMethodPatch() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("PATCH").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostNullAndMethodPost() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("POST")
                        .withPost(null).build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindPostNotStubbed_WhenPostEmptyAndMethodPost() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("POST")
                        .withPost("").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isFalse();
    }

    @Test
    public void shouldFindRequestBodyStubbed_WhenPostStubbedAndMethodPost() throws Exception {
        final StubRequest stubRequest =
                builder.withUrl("fssefewf")
                        .withMethod("POST")
                        .withPost("stubbed").build();

        assertThat(stubRequest.isRequestBodyStubbed()).isTrue();
    }
}
