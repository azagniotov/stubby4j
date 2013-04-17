package by.stub.yaml.stubs;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

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

   @BeforeClass
   public static void beforeClass() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }


   @Test
   public void shouldGetPostBody_WhenPostProvided_ButFileNotSet() throws Exception {

      final String url = "/invoice/789";

      final String postBody = "Hello";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withPost(postBody)
            .withMethodGet().build();

      assertThat(expectedRequest.getPostBody()).isEqualTo(postBody);
   }

   @Test
   public void shouldGetPostBody_WhenPostNotProvided_ButFileSet() throws Exception {

      final String url = "/invoice/789";

      final String fileContent = "Hello World!";
      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withFileBytes(fileContent.getBytes(StringUtils.utf8Charset()))
            .withMethodGet().build();

      assertThat(expectedRequest.getPostBody()).isEqualTo(fileContent);
   }

   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenDifferentHttpMethod() throws Exception {

      final String url = "/invoice/789";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

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
   public void stubbedRequestEqualsAssertingRequest_WhenAllHttpHeadersMatch() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenHeadersWereStubbed_ButNoHeadersSetToAssert() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenNoHeadersWereStubbed_ButHeadersWereSetToAssert() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllHeadersSubmittedCamelCased() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders("Content-Type", headerOneValue)
            .withHeaders("Content-Length", headerTwoValue)
            .withHeaders("Content-Language", headerThreeValue).build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenSomeHeadersMismatches() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, "application/json")
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestNotEqualsAssertingRequest_WhenNotAllHeadersSetToAssert() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }


   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenAllStubbedHeadersMatch() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest expectedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue)
            .withHeaders("content-encoding", "UTF-8")
            .withHeaders("Pragma", "no-cache").build();

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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
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
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
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

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

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
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue)
            .withHeaders("content-encoding", "UTF-8")
            .withHeaders("Pragma", "no-cache").build();

      assertThat(expectedRequest).isEqualTo(assertingRequest);
   }

   @Test
   public void stubbedRequestEqualsAssertingRequest_WhenThereLargeSetupOfStubbedProperties_ButNotAllHeadersSetToAssert() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[%22alex%22,%22tracy%22]";

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

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
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest assertingRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost(postBody)
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, "888")
            .withHeaders(headerThree, headerThreeValue)
            .withHeaders("content-encoding", "UTF-8")
            .withHeaders("Pragma", "no-cache").build();

      assertThat(expectedRequest).isNotEqualTo(assertingRequest);
   }
}