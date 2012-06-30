package org.stubby.server;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:23 AM
 */
public class JettyOrchestratorTest {

   private static JettyOrchestrator jettyOrchestrator;

   @BeforeClass
   public static void beforeClass() throws Exception {
      final Repository mockRepository = Mockito.mock(Repository.class);
      jettyOrchestrator = new JettyOrchestrator(mockRepository);
   }

   @Test
   public void shouldReturnDefaultClientPort() throws Exception {
      int clientPort = jettyOrchestrator.getClientPort(new HashMap<String, String>());
      Assert.assertEquals(JettyOrchestrator.DEFAULT_CLIENT_PORT, clientPort);
   }

   @Test
   public void shouldReturnGivenClientPort() throws Exception {
      final String expectedClientPort = "" + JettyOrchestrator.DEFAULT_CLIENT_PORT;
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, expectedClientPort);

      int clientPort = jettyOrchestrator.getClientPort(commandlineArgs);
      Assert.assertEquals(expectedClientPort, "" + clientPort);
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidClientPort() throws Exception {
      final String expectedClientPort = "12345aa";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, expectedClientPort);

      int clientPort = jettyOrchestrator.getClientPort(commandlineArgs);
      Assert.assertEquals(expectedClientPort, "" + clientPort);
   }

   @Test
   public void shouldReturnDefaultAdminPort() throws Exception {
      int adminPort = jettyOrchestrator.getAdminPort(new HashMap<String, String>());
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, adminPort);
   }

   @Test
   public void shouldReturnGivenAdminPort() throws Exception {
      final String expectedAdminPort = "" + JettyOrchestrator.DEFAULT_ADMIN_PORT;
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, expectedAdminPort);

      int adminPort = jettyOrchestrator.getAdminPort(commandlineArgs);
      Assert.assertEquals(expectedAdminPort, "" + adminPort);
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidAdminPort() throws Exception {
      final String expectedAdminPort = "12345as";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, expectedAdminPort);

      int adminPort = jettyOrchestrator.getAdminPort(commandlineArgs);
      Assert.assertEquals(expectedAdminPort, "" + adminPort);
   }
}