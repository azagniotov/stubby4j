package org.stubby.server;

import junit.framework.Assert;
import org.junit.Test;
import org.stubby.cli.CommandLineIntepreter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/24/12, 1:23 AM
 */
public class JettyOrchestratorTest {

   @Test
   public void shouldReturnDefaultClientPort() throws Exception {
      int clientPort = JettyOrchestrator.getClientPort(new HashMap<String, String>());
      Assert.assertEquals(JettyOrchestrator.DEFAULT_CLIENT_PORT, clientPort);
   }

   @Test
   public void shouldReturnGivenClientPort() throws Exception {
      final String expectedClientPort = "12345";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, expectedClientPort);

      int clientPort = JettyOrchestrator.getClientPort(commandlineArgs);
      Assert.assertEquals(expectedClientPort, "" + clientPort);
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidClientPort() throws Exception {
      final String expectedClientPort = "12345aa";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_CLIENTPORT, expectedClientPort);

      int clientPort = JettyOrchestrator.getClientPort(commandlineArgs);
      Assert.assertEquals(expectedClientPort, "" + clientPort);
   }

   @Test
   public void shouldReturnDefaultAdminPort() throws Exception {
      int adminPort = JettyOrchestrator.getAdminPort(new HashMap<String, String>());
      Assert.assertEquals(JettyOrchestrator.DEFAULT_ADMIN_PORT, adminPort);
   }

   @Test
   public void shouldReturnGivenAdminPort() throws Exception {
      final String expectedAdminPort = "12345";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, expectedAdminPort);

      int adminPort = JettyOrchestrator.getAdminPort(commandlineArgs);
      Assert.assertEquals(expectedAdminPort, "" + adminPort);
   }

   @Test(expected = NumberFormatException.class)
   public void shouldFailOnInvalidAdminPort() throws Exception {
      final String expectedAdminPort = "12345as";
      final Map<String, String> commandlineArgs = new HashMap<String, String>();
      commandlineArgs.put(CommandLineIntepreter.OPTION_ADMINPORT, expectedAdminPort);

      int adminPort = JettyOrchestrator.getAdminPort(commandlineArgs);
      Assert.assertEquals(expectedAdminPort, "" + adminPort);
   }
}