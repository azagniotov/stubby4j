package integration.by.stub.utils;

import junit.framework.Assert;
import org.eclipse.jetty.http.HttpSchemes;
import org.junit.Test;
import by.stub.exception.Stubby4JException;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 10:26 AM
 */
public class HandlerUtilsIT {

   @Test
   public void shouldGenerateServerName() throws Exception {
      final String serverName = HandlerUtils.constructHeaderServerName();
      Assert.assertEquals("stubby4j/x.x.x (Java-based HTTP stub server)", serverName);
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

   @Test
   public void shouldGetHtmlResourceByName() throws Exception {
      final String templateContent = HandlerUtils.getHtmlResourceByName("test-template");
      Assert.assertEquals("<html><head></head><body>%s</body></html>", templateContent);
   }

   @Test
   public void shouldPopulateHtmlTemplate() throws Exception {
      final String templateContent = HandlerUtils.populateHtmlTemplate("test-template", "alex");
      Assert.assertEquals("<html><head></head><body>alex</body></html>", templateContent);
   }

   @Test(expected = Stubby4JException.class)
   public void shouldNotPopulateNonExistentHtmlTemplate() throws Exception {
      HandlerUtils.populateHtmlTemplate("non-existent-template", "alex");
   }
}
