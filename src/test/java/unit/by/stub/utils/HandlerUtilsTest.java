package unit.by.stub.utils;

import by.stub.testing.junit.categories.UnitTest;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import junit.framework.Assert;
import org.eclipse.jetty.http.HttpSchemes;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 10:26 AM
 */
@Category(UnitTest.class)
public class HandlerUtilsTest {

   @Test
   public void shouldGenerateServerName() throws Exception {
      final String serverName = HandlerUtils.constructHeaderServerName();
      Assert.assertEquals("stubby4j/x.x.xx (HTTP stub server)", serverName);
   }

   @Test
   public void shouldEscapeHtmlEntities() throws Exception {
      final String escaped = StringUtils.escapeHtmlEntities("<xmlElement>8</xmlElement>");
      Assert.assertEquals("&lt;xmlElement&gt;8&lt;/xmlElement&gt;", escaped);
   }

   @Test
   public void shouldLinkifyUriString() throws Exception {
      final String linkified = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTP, "/google/1", "localhost", 8888);
      Assert.assertEquals("<a target='_blank' href='http://localhost:8888/google/1'>http://localhost:8888/google/1</a>", linkified);
   }

   @Test
   public void shouldLinkifyUriStringForHttps() throws Exception {
      final String linkified = HandlerUtils.linkifyRequestUrl(HttpSchemes.HTTPS, "/google/1", "localhost", 7443);
      Assert.assertEquals("<a target='_blank' href='https://localhost:7443/google/1'>https://localhost:7443/google/1</a>", linkified);
   }
}