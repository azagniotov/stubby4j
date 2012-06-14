package org.stubby.server;

import junit.framework.Assert;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletMapping;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.database.Repository;
import org.stubby.servlets.admin.PingServlet;
import org.stubby.servlets.admin.WelcomeServlet;
import org.stubby.servlets.client.ConsumerServlet;

/**
 * @author Alexander Zagniotov
 * @since 6/13/12, 10:32 PM
 */
public class JettyOrchestratorTest {

   @Test
   public void testClientPortNumber_whenArgsAreEmpty() throws Exception {
      final String[] someArgs = new String[]{};
      final int clientPort = JettyOrchestrator.getClientPort(someArgs);
      Assert.assertEquals(JettyOrchestrator.DEFAULT_CLIENT_PORT, clientPort);
   }

   @Test
   public void testAdminPortNumber_whenArgsAreEmpty() throws Exception {
      final String[] someArgs = new String[]{};
      final int adminPort = JettyOrchestrator.getAdminPort(someArgs);
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, adminPort);
   }

   @Test
   public void testClientPortNumber_whenArgsHaveOneArbitaryArg() throws Exception {
      final String[] someArgs = new String[]{"blah"};
      final int clientPort = JettyOrchestrator.getClientPort(someArgs);
      Assert.assertEquals(JettyOrchestrator.DEFAULT_CLIENT_PORT, clientPort);
   }

   @Test
   public void testAdminPortNumber_whenArgsHaveOneArbitaryArg() throws Exception {
      final String[] someArgs = new String[]{"blah"};
      final int adminPort = JettyOrchestrator.getAdminPort(someArgs);
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, adminPort);
   }

   @Test
   public void testClientPortNumber_whenArgsHaveYAMLPathAndOneNumericArg() throws Exception {
      final String[] someArgs = new String[]{"Potential YAML Path", "456"};
      final int clientPort = JettyOrchestrator.getClientPort(someArgs);
      Assert.assertEquals(456, clientPort);
   }

   @Test
   public void testAdminPortNumber_whenArgsHaveTwoNumericArg() throws Exception {
      final String[] someArgs = new String[]{"123", "456"};
      final int adminPort = JettyOrchestrator.getAdminPort(someArgs);
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, adminPort);
   }

   @Test
   public void testBothPortNumbers_whenArgsHaveTwoNumericArg() throws Exception {
      final String[] someArgs = new String[]{"Potential YAML Path", "123", "456"};
      final int clientPort = JettyOrchestrator.getClientPort(someArgs);
      final int adminPort = JettyOrchestrator.getAdminPort(someArgs);
      Assert.assertEquals(123, clientPort);
      Assert.assertEquals(456, adminPort);
   }

   @Test
   public void testCreateClientContextHandler_givenConnectorName() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createClientContextHandler(mockRepository);

      Assert.assertEquals(JettyOrchestrator.CLIENT_CONNECTOR_NAME, servletContextHandler.getConnectorNames()[0]);
   }

   @Test
   public void testCreateAdminContextHandler_givenConnectorName() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createAdminContextHandler(mockRepository);

      Assert.assertEquals(JettyOrchestrator.ADMIN_CONNECTOR_NAME, servletContextHandler.getConnectorNames()[0]);
   }

   @Test
   public void testCreateClientContextHandler_givenContextPath() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createClientContextHandler(mockRepository);

      Assert.assertEquals("/", servletContextHandler.getContextPath());
   }

   @Test
   public void testCreateAdminContextHandler_givenContextPath() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createAdminContextHandler(mockRepository);

      Assert.assertEquals("/", servletContextHandler.getContextPath());
   }

   @Test
   public void testCreateClientContextHandler_servletName() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createClientContextHandler(mockRepository);

      final ServletMapping servletMapping =
            servletContextHandler.getServletHandler().getServletMapping(JettyOrchestrator.GLOBAL_CONTEXT_PATH);
      final String servletName = servletMapping.getServletName();
      Assert.assertEquals(ConsumerServlet.class.getSimpleName(), servletName);
   }

   @Test
   public void testCreateAdminContextHandler_welcomeServletName() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createAdminContextHandler(mockRepository);

      final ServletMapping servletMapping =
            servletContextHandler.getServletHandler().getServletMapping(JettyOrchestrator.GLOBAL_CONTEXT_PATH);
      final String servletName = servletMapping.getServletName();
      Assert.assertEquals(WelcomeServlet.class.getSimpleName(), servletName);
   }

   @Test
   public void testCreateAdminContextHandler_pingServletName() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      final ServletContextHandler servletContextHandler = (ServletContextHandler) JettyOrchestrator.createAdminContextHandler(mockRepository);

      final ServletMapping servletMapping =
            servletContextHandler.getServletHandler().getServletMapping(JettyOrchestrator.ADMIN_PING_CONTEXT_PATH);
      final String servletName = servletMapping.getServletName();
      Assert.assertEquals(PingServlet.class.getSimpleName(), servletName);
   }
}
