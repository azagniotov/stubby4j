package by.stub.yaml.stubs;

import by.stub.builder.stubs.StubRequestBuilder;
import by.stub.cli.CommandLineInterpreter;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
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
   public void shouldNotMatchStubRequest_WhenDifferentMethod() throws Exception {

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenDifferentPostBody() throws Exception {

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("different post").build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenPostBodyWasStubbed_ButNoPostBodySubmitted() throws Exception {

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }

   @Test
   public void shouldMatchStubRequest_WhenNoPostBodyWasStubbed_ButPostBodyWasSubmitted() throws Exception {

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodPost().build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodPost()
            .withPost("some post").build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenDifferentUrl() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl("/invoice/788")
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramTwo, paramTwoValue)
            .withQuery(paramOne, paramOneValue).build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }


   @Test
   public void shouldMatchStubRequest_WhenAllHeadersMatch() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldNotMatchStubRequest_WhenHeadersWereStubbed_ButNoHeadersSubmitted() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }


   @Test
   public void shouldMatchStubRequest_WhenNoHeadersWereStubbed_ButHeadersWereSubmitted() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet().build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldMatchStubRequest_WhenAllHeadersSubmittedCamelCased() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders("Content-Type", headerOneValue)
            .withHeaders("Content-Length", headerTwoValue)
            .withHeaders("Content-Language", headerThreeValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldNotMatchStubRequest_WhenSomeHeadersMismatch() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, "application/json")
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }


   @Test
   public void shouldNotMatchStubRequest_WhenNotAlHeadersSubmitted() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }


   @Test
   public void shouldMatchStubRequest_WhenAllStubbedHeadersMatch() throws Exception {

      final String headerOne = "content-type";
      final String headerOneValue = "application/xml";

      final String headerTwo = "content-length";
      final String headerTwoValue = "30";

      final String headerThree = "content-language";
      final String headerThreeValue = "en-US";

      final String url = "/invoice/123";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withHeaders(headerOne, headerOneValue)
            .withHeaders(headerTwo, headerTwoValue)
            .withHeaders(headerThree, headerThreeValue)
            .withHeaders("content-encoding", "UTF-8")
            .withHeaders("Pragma", "no-cache").build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldMatchStubRequest_WhenNoQueryParamsWereStubbed_ButQueryParamsWereSubmitted() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead().build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenQueryParamsWereStubbed_ButNoQueryParamsWereSubmitted() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead().build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }


   @Test
   public void shouldMatchStubRequest_WhenAllQueryParamsMatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldMatchStubRequest_WhenAllStubbedQueryParamsMatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withQuery("paramThree", "three").build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }


   @Test
   public void shouldNotMatchStubRequest_WhenNotAllQueryParamsSubmitted() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue)
            .withQuery("paramThree", "three").build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenQueryParamsMismatch() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, "three").build();

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }

   @Test
   public void shouldMatchStubRequest_WhenQueryParamsInDifferentOrder() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "two";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramTwo, paramTwoValue)
            .withQuery(paramOne, paramOneValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldMatchStubRequest_WhenQueryParamIsArray() throws Exception {

      final String paramOne = "paramOne";
      final String paramOneValue = "one";

      final String paramTwo = "paramTwo";
      final String paramTwoValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      final StubRequest actualRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue)
            .withQuery(paramTwo, paramTwoValue).build();

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldMatchStubRequest_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

      final StubRequest actualRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldMatchStubRequest_WhenQueryParamUrlEncodedArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\",\"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=%5B%22alex%22,%22tracy%22%5D");

      final StubRequest actualRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(actualRequest, is(equalTo(stubbedRequest)));
   }

   @Test
   public void shouldNotMatchStubRequest_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes_ButDifferentSpacing() throws Exception {

      final String paramOne = "names";
      final String paramOneValue = "[\"alex\", \"tracy\"]";

      final String url = "/invoice/789";

      final StubRequest stubbedRequest =
         BUILDER.withUrl(url)
            .withMethodGet()
            .withMethodHead()
            .withQuery(paramOne, paramOneValue).build();

      final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
      when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
      when(mockHttpServletRequest.getMethod()).thenReturn("GET");
      when(mockHttpServletRequest.getQueryString()).thenReturn("names=[%22alex%22,%22tracy%22]");

      final StubRequest actualRequest = StubRequest.createFromHttpServletRequest(mockHttpServletRequest);

      assertThat(actualRequest, is(not(equalTo(stubbedRequest))));
   }
}