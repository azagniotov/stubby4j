package org.stubby.server;

import junit.framework.Assert;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.DataStore;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:23 AM
 */
public class JettyOrchestratorTest {

   private static JettyOrchestrator jettyOrchestrator;
   private final Server spyServer = Mockito.spy(new Server());
   private final DataStore mockDataStore = Mockito.mock(DataStore.class);

   @Before
   public void beforeTest() throws Exception {
      doNothing().when(spyServer).setConnectors(Mockito.any(Connector[].class));
      doNothing().when(spyServer).setHandler(Mockito.any(HandlerList.class));
      doNothing().when(spyServer).start();
   }

   @After
   public void afterTest() throws Exception {
      if (jettyOrchestrator != null) {
         jettyOrchestrator.stopJetty();
         jettyOrchestrator = null;
      }
   }

   @Test
   public void shouldReturnDefaultClientPort() throws Exception {
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, new HashMap<String, String>());
      jettyOrchestrator.startJetty();
      Assert.assertEquals(JettyOrchestrator.DEFAULT_CLIENT_PORT, jettyOrchestrator.getCurrentClientPort());
   }

   @Test
   public void shouldReturnGivenClientPort() throws Exception {

      final int expectedClientPort = 1244;

      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, Integer.toString(expectedClientPort));
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, commandlineArgs);
      jettyOrchestrator.startJetty();

      Assert.assertEquals(expectedClientPort, jettyOrchestrator.getCurrentClientPort());
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidClientPort() throws Exception {
      final String expectedClientPort = "1244asd";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, new Integer(expectedClientPort).toString());
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, commandlineArgs);
      jettyOrchestrator.startJetty();
   }

   @Test
   public void shouldReturnDefaultAdminPort() throws Exception {
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, new HashMap<String, String>());
      jettyOrchestrator.startJetty();
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, jettyOrchestrator.getCurrentAdminPort());
   }

   @Test
   public void shouldReturnGivenAdminPort() throws Exception {

      final int expectedAdminPort = 1245;
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, Integer.toString(expectedAdminPort));
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, commandlineArgs);
      jettyOrchestrator.startJetty();

      Assert.assertEquals(expectedAdminPort, jettyOrchestrator.getCurrentAdminPort());
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidAdminPort() throws Exception {
      final String expectedAdminPort = "1244asd";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, new Integer(expectedAdminPort).toString());
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, commandlineArgs);
      jettyOrchestrator.startJetty();
   }

   @Test
   public void shouldReturnDefaultHost() throws Exception {
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, new HashMap<String, String>());
      jettyOrchestrator.startJetty();
      Assert.assertEquals(JettyOrchestrator.DEFAULT_HOST, jettyOrchestrator.getCurrentHost());
   }

   @Test
   public void shouldReturnGivenHost() throws Exception {

      final String expectedHost = "132.122.123.125";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADDRESS, expectedHost);
      jettyOrchestrator = new JettyOrchestrator(spyServer, mockDataStore, commandlineArgs);
      jettyOrchestrator.startJetty();

      Assert.assertEquals(expectedHost, jettyOrchestrator.getCurrentHost());
   }
}