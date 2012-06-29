/*
A Java-based HTTP stub server

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.stubby;

import org.apache.commons.cli.ParseException;
import org.stubby.cli.CommandLineIntepreter;
import org.stubby.database.Repository;
import org.stubby.server.JettyOrchestrator;
import org.stubby.yaml.YamlConsumer;
import org.stubby.yaml.stubs.StubHttpLifecycle;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class Stubby4JRunner {


   public static Map<String, String> doGetOnURI(final String uri) throws Exception {
      final Map<String, String> results = new HashMap<String, String>();

      final String urlString = String.format("http://%s:%s%s", JettyOrchestrator.currentHost, JettyOrchestrator.currentClientPort, uri);
      final URL url = new URL(urlString);
      final HttpURLConnection con = (HttpURLConnection) url.openConnection();
      final Integer responseCode = con.getResponseCode();
      final String response = new Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A").next();
      results.put("status", responseCode.toString());
      results.put("response", response.trim());
      return results;
   }

   public static void startStubby4J(final InputStream yamlInputStream) throws Exception {
      startStubby4J(yamlInputStream, JettyOrchestrator.currentClientPort, JettyOrchestrator.currentAdminPort);
   }

   public static void stopStubby4J() throws Exception {
      JettyOrchestrator.stopJetty();
   }

   public static void startStubby4J(final InputStream yamlInputStream, final int clientPort, final int adminPort) throws Exception {
      final List<StubHttpLifecycle> httpLifecycles = YamlConsumer.readYaml(yamlInputStream);
      final Map<String, String> params = new HashMap<String, String>();
      params.put(CommandLineIntepreter.OPTION_CLIENTPORT, String.format("%s", clientPort));
      params.put(CommandLineIntepreter.OPTION_ADMINPORT, String.format("%s", adminPort));
      JettyOrchestrator.startJetty(new Repository(httpLifecycles), params);
   }

   public static void main(final String[] args) {

      try {
         CommandLineIntepreter.parseCommandLine(args);
      } catch (ParseException e) {
         e.printStackTrace();
         System.exit(1);
      }
      if (CommandLineIntepreter.isHelp()) {
         CommandLineIntepreter.printHelp(Stubby4JRunner.class);

      } else if (!CommandLineIntepreter.isYamlProvided()) {
         System.err.println("\n\nYAML configuration was not provided using command line option '-f' or '--config'.\nPlease run again with option '--help'\n\n");
         System.exit(1);

      } else {

         try {
            final Map<String, String> params = CommandLineIntepreter.getCommandlineParams();
            final String yamlConfigFilename = params.get(CommandLineIntepreter.OPTION_CONFIG);
            final List<StubHttpLifecycle> httpLifecycles = YamlConsumer.readYaml(yamlConfigFilename);
            JettyOrchestrator.startJetty(new Repository(httpLifecycles), params);

         } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }
   }
}