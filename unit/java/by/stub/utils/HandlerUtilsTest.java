package by.stub.utils;

import org.eclipse.jetty.http.HttpSchemes;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 10:26 AM
 */

public class HandlerUtilsTest {

   @Test
   public void shouldEscapeHtmlEntities() throws Exception {

      final String actualEscaped = StringUtils.escapeHtmlEntities("<xmlElement>8</xmlElement>");
      final String expectedEscaped = "&lt;xmlElement&gt;8&lt;/xmlElement&gt;";

      assertThat(actualEscaped).isEqualTo(expectedEscaped);
   }

   @Test
   public void shouldLinkifyUriString() throws Exception {

      final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, "/google/1", "localhost", 8888);
      final String expectedLinkified = "<a target='_blank' href='http://localhost:8888/google/1'>http://localhost:8888/google/1</a>";

      assertThat(actualLinkified).isEqualTo(expectedLinkified);
   }

   @Test
   public void shouldLinkifyUriStringForHttps() throws Exception {

      final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, "/google/1", "localhost", 7443);
      final String expectedLinkified = "<a target='_blank' href='https://localhost:7443/google/1'>https://localhost:7443/google/1</a>";

      assertThat(actualLinkified).isEqualTo(expectedLinkified);
   }

   @Test
   public void shouldLinkifyUriStringWithSingleQuotesInQueryString() throws Exception {

      final String actualLinkified = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, "/entity?client_secret=secret&attributes=['id','uuid']", "localhost", 8882);
      final String expectedLinkified = "<a target='_blank' href='http://localhost:8882/entity?client_secret=secret&attributes=[%27id%27,%27uuid%27]'>http://localhost:8882/entity?client_secret=secret&attributes=['id','uuid']</a>";

      assertThat(actualLinkified).isEqualTo(expectedLinkified);
   }
}
