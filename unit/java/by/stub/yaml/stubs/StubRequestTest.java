package by.stub.yaml.stubs;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.utils.FileUtils;
import com.google.api.client.http.HttpMethods;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 3:44 PM
 */
@SuppressWarnings("serial")
public class StubRequestTest {

   private static final StubRequestBuilder BUILDER = new StubRequestBuilder();

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNullUrlStubbed_AndNullUrlSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl(null).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(null).withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenEmptyUrlStubbed_AndEmptyUrlSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNullUrlStubbed_ButUrlSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl(null).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlStubbed_ButNullUrlSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(null).withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlStubbed_ButNoUrlSubmitted() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlNotStubbed_ButUrlSubmitted() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest = BUILDER.withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(url).withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenRootUrlsEquals() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedRootUrlStartsWithRegex_ButSubmittedUrlRoot() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("^/$").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlHasOptionalTrailingSlash_ButNoSlashSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123/?").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlHasOptionalTrailingSlash_ButSlashSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123/?").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenStubbedUrlHasRequiredTrailingSlash_ButNoSlashSubmitted() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123/").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexExact_AndUrlsEqual() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("^/invoice/123$").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexBeginsWith() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("^/invoice/123.*").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/12345").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenSubmittedUrlLonger() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/12345").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenStubbedUrlRegexBeginsWith_AndSubmittedUrlWrong() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("^/invoice/123").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/1").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenSubmittedUrlShorter() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/1").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlsEquals() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexEndsWith_AndSubmittedUrlHasExtraBeggining() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl(".*/invoice/123$").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/some/beggining/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexAnythingAround_AndUrlsEqual() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl(".*/invoice/123.*").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenStubbedUrlRegexGroups_AndUrlsEqual() throws Exception {

      final StubRequest expectedRequest = BUILDER.withUrl("(.*)(/invoice/123)(.*)").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/invoice/123").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentUri() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl("/invoice/788")
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramTwo, paramTwoValue)
            .withQuery(paramOne, paramOneValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenMethodStubbed_ButLowerCasedMethodSubmitted() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(url).withMethod("get").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenMethodStubbed_ButNoMethodSubmitted() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(url).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNoMethodStubbed_ButMethodSubmitted() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest = BUILDER.withUrl(url).build();
      final StubRequest assertingRequest = BUILDER.withUrl(url).withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void shouldGetPostBody_WhenPostProvided_ButFileIsNull() throws Exception {

      final String url = "/invoice/789";

      final String postBody = "Hello";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withPost(postBody)
            .withMethodGet().build();

      assertThat(expectedRequest.getPostBody()).isEqualTo(postBody);
   }

   @Test
   public void shouldGetPostBody_WhenPostProvided_ButFileIsEmpty() throws Exception {

      final String url = "/invoice/789";

      final String postBody = "Hello";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withPost(postBody)
            .withFile(null)
            .withMethodGet().build();

      assertThat(expectedRequest.getPostBody()).isEqualTo(postBody);
   }

   @Test
   public void shouldGetPostBody_WhenPostNotProvided_ButFileSet() throws Exception {

      final String url = "/invoice/789";

      final String fileContent = "Hello World!";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withFile(FileUtils.fileFromString(fileContent))
            .withMethodGet().build();

      assertThat(expectedRequest.getPostBody()).isEqualTo(fileContent);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentHttpMethod() throws Exception {

      final String url = "/invoice/789";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl(url).withMethodPost().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentPostBody() throws Exception {

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("different post").build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenPostBodyWasStubbed_ButNoPostBodySubmitted() throws Exception {

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNoPostBodyWasStubbed_ButPostBodyWasSubmitted() throws Exception {

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllHttpHeadersMatch() throws Exception {

      final String url = "/invoice/123";
      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenHeadersWereStubbed_ButNoHeadersSetToAssert() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType("application/xml")
            .withHeaderContentLength("30")
            .withHeaderContentLanguage("en-US").build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNoHeadersWereStubbed_ButHeadersWereSetToAssert() throws Exception {

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType("application/xml")
            .withHeaderContentLength("30")
            .withHeaderContentLanguage("en-US").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllHeadersSubmittedCamelCased() throws Exception {

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders("Content-Type", contentType)
            .withHeaders("Content-Length", contentLength)
            .withHeaders("Content-Language", contentLanguage).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllHeadersStubbedCamelCased() throws Exception {

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders("Content-Type", contentType)
            .withHeaders("Content-Length", contentLength)
            .withHeaders("Content-Language", contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();


      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenSomeHeadersMismatches() throws Exception {

      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType("application/xml")
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType("application/json")
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenNotAllHeadersSetToAssert() throws Exception {

      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType("application/xml")
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllStubbedHeadersMatch() throws Exception {

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";
      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage)
            .withHeaderContentEncoding("UTF-8")
            .withHeaderPragma("no-cache").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNoQueryParamsWereStubbed_ButQueryParamsWereSetToAssert() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead().build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamsWereStubbed_ButNoQueryParamsWereSetToAssert() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllQueryParamsMatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllStubbedQueryParamsMatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withQuery("paramThree", "three").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenNotAllQueryParamsSetToAssert() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withQuery("paramThree", "three").build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamsMismatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, "three").build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenQueryParamsInDifferentOrder() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramTwo, paramTwoValue)
            .withQuery(paramOne, paramOneValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenQueryParamIsArray() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenQueryParamUrlEncodedArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=%5B%22alex%22,%22tracy%22%5D");

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenQueryParamUrlEncodedArrayHasElementsWithinUrlEncodedSingleQuotes() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "['alex','tracy']";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=%5B%27alex%27,%27tracy%27%5D");

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenQueryParamArrayElementsHaveDifferentSpacing() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\", \"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

      final StubRequest assertingRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenThereLargeSetupOfStubbedProperties() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";

      final String url = "/invoice/123";
      final String postBody = "this is a post body";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
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
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage)
            .withHeaderContentEncoding("UTF-8")
            .withHeaderPragma("no-cache").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoAssertingValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoAssertingValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenPostRegexDoesNotMatchLineChar() throws Exception {

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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postAssertingValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithLineChar() throws Exception {

      final String postRegex = "^[\\.,'a-zA-Z\\s+]*$";

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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingPostWithCarriageReturnChar() throws Exception {

      final String postRegex = "^[\\.,'a-zA-Z\\s+]*$";

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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
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
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postAssertingValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenPostRegexMatchingSingleLinePost() throws Exception {

      final String postRegex = "^This is an invoice: \\d{3} from today";
      final String postValue = "This is an invoice: 889 from today";

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postRegex).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withPost(postValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenThereLargeSetupOfStubbedProperties_ButNotAllHeadersSetToAssert() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";

      final String url = "/invoice/123";
      final String postBody = "this is a post body";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
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
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength("888")
            .withHeaderContentLanguage(contentLanguage)
            .withHeaderContentEncoding("UTF-8")
            .withHeaderPragma("no-cache").build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenRegexifiedUrlDoesNotBeginWithRegexSign_ItsNotProcessedAsRegex() throws Exception {

      final String url = ".*account.*";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/some/products/account/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenRegexifiedUrlBeginsWithRegexSign_ItsProcessedAsRegex() throws Exception {

      final String url = "^.*account.*";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/some/products/account/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlPartiallyRegexified_ButGoodAssertionUrlConfigured() throws Exception {

      final String url = "^/products/[0-9]+/?$";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlRegexified_ButGoodAssertionUrlConfigured() throws Exception {

      final String url = "^/[a-z]{3}/[0-9]+/?$";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/abc/12345/").withMethodGet().build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlConditionallyRegexified_ButGoodAssertionUrlConfigured() throws Exception {

      final String url = "^/(cats|dogs)/?(.*)";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER.withUrl("/cats/blah/again/").withMethodGet().build());
         add(BUILDER.withUrl("/cats/blah/").withMethodGet().build());
         add(BUILDER.withUrl("/dogs/blah/").withMethodGet().build());
         add(BUILDER.withUrl("/dogs/").withMethodGet().build());
         add(BUILDER.withUrl("/dogs").withMethodGet().build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isEqualTo(assertingRequest);
      }
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlConditionallyRegexified_ButGoodAssertionUrlConfigured_v2() throws Exception {

      final String url = "^/(account|profile)/user/session/[a-zA-Z0-9]{32}/?";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER.withUrl("/account/user/session/d41d8cd98f00b204e9800998ecf8427e").withMethodGet().build());
         add(BUILDER.withUrl("/account/user/session/d41d8cd98f00b204e9800998ecf8427e/").withMethodGet().build());
         add(BUILDER.withUrl("/profile/user/session/d41d8cd98f00b204e9800998ecf8427e").withMethodGet().build());
         add(BUILDER.withUrl("/profile/user/session/d41d8cd98f00b204e9800998ecf8427e/").withMethodGet().build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isEqualTo(assertingRequest);
      }
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexified_ButBadAssertionUrlConfigured() throws Exception {

      final String url = "^/[a-z]{3}/[0-9]+/?$";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/abcm/12345/").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenComplexUrlRegexified_ButGoodAssertionUrlConfigured() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER.withUrl("/abc-efg/12/KM/jhgjkhg234234l2").withMethodGet().build());
         add(BUILDER.withUrl("/abc-efg/12/KM/23423").withMethodGet().build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerty").withMethodGet().build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isEqualTo(assertingRequest);
      }
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenComplexUrlRegexified_ButBadAssertionUrlConfigured() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+$";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER.withUrl("/abca-efg/12/KM/jhgjkhg234234l2").withMethodGet().build());
         add(BUILDER.withUrl("/abcefg/12/KM/23423").withMethodGet().build());
         add(BUILDER.withUrl("/aaa-aaa/00/Af/qwerty").withMethodGet().build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerTy").withMethodGet().build());
         add(BUILDER.withUrl("/aaa-aaa/009/AA/qwerty").withMethodGet().build());
         add(BUILDER.withUrl("/AAA-AAA/00/AA/qwerty").withMethodGet().build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isNotEqualTo(assertingRequest);
      }
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenUrlRegexifiedDoesNotAccomodateForQueryString() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER
            .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/abc-efg/12/KM/23423")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerty")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isEqualTo(assertingRequest);
      }
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedHasQueryString() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{8}&paramTwo=[a-zA-Z]{8}";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER
            .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
            .withMethodGet()
            .withQuery("paramOne", "wqePwrew")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/abc-efg/12/KM/23423")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerty")
            .withMethodGet()
            .withQuery("paramOne", "aaaaaaaa")
            .withQuery("paramTwo", "QwErTyUi").build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isNotEqualTo(assertingRequest);
      }
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedWithStaticQueryString() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=valueOne&paramTwo=valueTwo";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER
            .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/abc-efg/12/KM/23423")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerty")
            .withMethodGet()
            .withQuery("paramOne", "valueOne")
            .withQuery("paramTwo", "valueTwo").build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isNotEqualTo(assertingRequest);
      }
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenUrlRegexifiedAccomodatesForQueryString_ButBadAssertionUrlConfigured() throws Exception {

      final String url = "^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{8}&paramTwo=[a-zA-Z]{8}";

      final StubRequest expectedRequest = BUILDER.withUrl(url).withMethodGet().build();

      final List<StubRequest> assertingRequests = new LinkedList<StubRequest>() {{
         add(BUILDER
            .withUrl("/abc-efg/12/KM/jhgjkhg234234l2")
            .withMethodGet()
            .withQuery("paramSix", "wqePwrew")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/abc-efg/12/KM/23423")
            .withMethodGet()
            .withQuery("paramOne", "12345678")
            .withQuery("paramTwo", "valueTwo").build());
         add(BUILDER.withUrl("/aaa-aaa/00/AA/qwerty")
            .withMethodGet()
            .withQuery("paramOne", "aaa7aaaa")
            .withQuery("paramTwo", "QwErTyUi").build());
      }};

      for (final StubRequest assertingRequest : assertingRequests) {
         assertThat(expectedRequest).isNotEqualTo(assertingRequest);
      }
   }

   @Test
   public void shouldfindStubRequestNotEqual_WhenComparedToNull() throws Exception {
      final StubRequest expectedRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(null);
   }

   @Test
   public void shouldfindStubRequestNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
      final StubRequest expectedRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();
      final Object assertingObject = StubResponse.newStubResponse();

      final boolean assertionResult = expectedRequest.equals(assertingObject);
      assertThat(assertionResult).isFalse();
   }

   @Test
   public void shouldfindStubRequestEqual_WhenComparedToSameInstanceClass() throws Exception {
      final StubRequest expectedRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();
      final Object assertingObject = StubRequest.newStubRequest();

      final boolean assertionResult = assertingObject.equals(expectedRequest);
      assertThat(assertionResult).isFalse();
   }

   @Test
   public void shouldfindStubRequestEqual_WhenComparedToDifferentObjectWithSameProperties() throws Exception {
      final StubRequest expectedRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();
      final StubRequest assertingRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();

      final boolean assertionResultOne = assertingRequest.equals(expectedRequest);
      final boolean assertionResultTwo = expectedRequest.equals(assertingRequest);

      assertThat(assertionResultOne).isTrue();
      assertThat(assertionResultTwo).isTrue();
   }

   @Test
   public void shouldfindStubRequestEqual_WhenComparedToSameIdentity() throws Exception {
      final StubRequest expectedRequest = BUILDER.withUrl("/products/12345/").withMethodGet().build();
      final StubRequest assertingRequest = expectedRequest;

      assertThat(assertingRequest).isEqualTo(expectedRequest);
   }


   @Test
   public void shouldFindTwoHashCodesEqual_WhenTwoRequestAreTheSame() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";

      final String url = "/invoice/123";
      final String postBody = "this is a post body";

      final StubRequest requestOne =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(postBody)
            .withFile(FileUtils.fileFromString("bytes"))
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest requestTwo =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(postBody)
            .withFile(FileUtils.fileFromString("bytes"))
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
   }

   @Test
   public void shouldNotFindTwoHashCodesEqual_WhenTwoRequestHaveDifferentAmdNullPostBody() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";

      final String url = "/invoice/123";
      final String postBody = "this is a post body";

      final StubRequest requestOne =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(null)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest requestTwo =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(requestOne.hashCode()).isNotEqualTo(requestTwo.hashCode());
   }

   @Test
   public void shouldNotFindTwoHashCodesEqual_WhenTwoRequestHaveDifferentHeaderValue() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String contentType = "application/xml";
      final String contentLength = "30";
      final String contentLanguage = "en-US";

      final String url = "/invoice/123";
      final String postBody = "this is a post body";

      final StubRequest requestOne =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength(contentLength)
            .withHeaderContentLanguage(contentLanguage).build();

      final StubRequest requestTwo =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaderContentType(contentType)
            .withHeaderContentLength("31")
            .withHeaderContentLanguage(contentLanguage).build();

      assertThat(requestOne.hashCode()).isNotEqualTo(requestTwo.hashCode());
   }

   @Test
   public void shouldFindTwoHashCodesEqual_WhenTwoRequestHaveMethodAndUrlNull() throws Exception {

      final StubRequest requestOne =
         BUILDER.withUrl(null)
            .withMethod(null).build();

      final StubRequest requestTwo =
         BUILDER.withUrl(null)
            .withMethod(null).build();

      assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
   }

   @Test
   public void shouldFindTwoHashCodesEqual_WhenTwoRequestHaveUrlNull() throws Exception {

      final StubRequest requestOne =
         BUILDER.withUrl(null)
            .withMethodGet().build();

      final StubRequest requestTwo =
         BUILDER.withUrl(null)
            .withMethodGet().build();

      assertThat(requestOne.hashCode()).isEqualTo(requestTwo.hashCode());
   }

   @Test
   public void shouldMatchExpectedToStringOutput_WhenActualRequestHasTheSameOutput() throws Exception {

      final StubRequest actualRequest =
         BUILDER.withUrl("/invoice/123")
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost("this is a post body")
            .withQuery("paramOne", "paramOneValue")
            .withQuery("paramTwo", "paramTwoValue")
            .withHeaders("headerOne", "headerOneValue")
            .withHeaders("headerTwo", "headerTwoValue")
            .withHeaders("headerThree", "headerThreeValue").build();


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
         BUILDER.withUrl("/invoice/123")
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(null)
            .withQuery("paramOne", "paramOneValue")
            .withQuery("paramTwo", "paramTwoValue")
            .withHeaders("headerOne", "headerOneValue")
            .withHeaders("headerTwo", "headerTwoValue")
            .withHeaders("headerThree", "headerThreeValue").build();


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
         BUILDER.withUrl("/invoice/123")
            .withMethodGet()
            .withMethodPost()
            .withMethodPut()
            .withPost(null)
            .withQuery("paramOne", "paramOneValue")
            .withQuery("paramTwo", "paramTwoValue")
            .withHeaders("headerOne", null)
            .withHeaders("headerTwo", "headerTwoValue")
            .withHeaders("headerThree", "headerThreeValue").build();


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
         BUILDER.withUrl(null)
            .withMethod(null)
            .withPost(null).build();


      final String expectedToStringOutput = "StubRequest{" +
         "url=null, " +
         "method=[null], " +
         "query={}, " +
         "headers={}}";

      assertThat(actualRequest.toString()).isEqualTo(expectedToStringOutput);
   }
}