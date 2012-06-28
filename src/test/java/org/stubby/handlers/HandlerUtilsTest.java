package org.stubby.handlers;

import junit.framework.Assert;
import org.junit.Test;
import org.stubby.exception.Stubby4JException;

/**
 * @author Alexander Zagniotov
 * @since 6/27/12, 10:26 AM
 */
public class HandlerUtilsTest {

   @Test
   public void shouldGenerateServerName() throws Exception {
      final String serverName = HandlerUtils.constructHeaderServerName();
      Assert.assertEquals("stubby4j/x.x.x (Java-based HTTP stub server)", serverName);
   }

   @Test
   public void shouldEscapeHtmlEntities() throws Exception {
      final String escaped = HandlerUtils.escapeHtmlEntities("<xmlElement>8</xmlElement>");
      Assert.assertEquals("&lt;xmlElement&gt;8&lt;/xmlElement&gt;", escaped);
   }

   @Test
   public void shouldLinkifyUriString() throws Exception {
      final String linkified = HandlerUtils.linkifyRequestUrl("/google/1");
      Assert.assertEquals("<a target='_blank' href='http://localhost:8882/google/1'>/google/1</a>", linkified);
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
