package org.stubby.cli;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Alexander Zagniotov
 * @since 6/13/12, 10:20 PM
 */
public class CommandLineIntepreterTest {

   @Test
   public void testIsHelp_whenArgsAreEmpty() throws Exception {
      final String[] someArgs = new String[] {};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(true, result);
   }

   @Test
   public void testIsHelp_whenArgsHaveDashH() throws Exception {
      final String[] someArgs = new String[] {"-h"};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(true, result);
   }

   @Test
   public void testIsHelp_whenArgsHaveDashDashHelp() throws Exception {
      final String[] someArgs = new String[] {"--help"};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(true, result);
   }

   @Test
   public void testIsHelp_whenArgsHaveArbitaryArgument() throws Exception {
      final String[] someArgs = new String[] {"blah"};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(false, result);
   }

   @Test
   public void testIsHelp_whenArgsHaveDashDashHelpPlusArbitaryArgument() throws Exception {
      final String[] someArgs = new String[] {"--help", "blah"};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(false, result);
   }

   @Test
   public void testIsHelp_whenArgsHaveArbitaryArgumentPlusDashDashHelp() throws Exception {
      final String[] someArgs = new String[] {"blah", "--help"};
      final boolean result = CommandLineIntepreter.isHelp(someArgs);
      Assert.assertEquals(false, result);
   }
}