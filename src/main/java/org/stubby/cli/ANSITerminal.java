/*
HTTP stub server written in Java with embedded Jetty

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

package org.stubby.cli;

public final class ANSITerminal {

   private ANSITerminal() {

   }

   private static boolean mute = false;

   private static final char ESCAPE = 27;

   private static final String BOLD = String.format("%s[1m", ESCAPE);
   private static final String RESET = String.format("%s[0m", ESCAPE);

   private static final String BLACK = String.format("%s[30m", ESCAPE);
   private static final String BOLD_BLACK = String.format("%s%s", BOLD, BLACK);
   private static final String BLUE = String.format("%s[34m", ESCAPE);
   private static final String CYAN = String.format("%s[36m", ESCAPE);
   private static final String GREEN = String.format("%s[32m", ESCAPE);
   private static final String MAGENTA = String.format("%s[35m", ESCAPE);
   private static final String RED = String.format("%s[31m", ESCAPE);
   private static final String YELLOW = String.format("%s[33m", ESCAPE);

   private static void print(final String color, final String msg) {
      if (mute) {
         return;
      }
      System.out.println(String.format("%s%s%s", color, msg, RESET));
   }

   public static void log(final String msg) {
      print("", msg);
   }

   public static void dump(final String msg) {
      log(msg);
   }

   public static void status(final String msg) {
      print(BOLD_BLACK, msg);
   }

   public static void info(final String msg) {
      print(BLUE, msg);
   }

   public static void ok(final String msg) {
      print(GREEN, msg);
   }

   public static void error(final String msg) {
      print(RED, msg);
   }

   public static void warn(final String msg) {
      print(YELLOW, msg);
   }

   public static void incoming(final String msg) {
      print(CYAN, msg);
   }

   public static void loaded(final String msg) {
      print(MAGENTA, msg);
   }

   public static void muteConsole(final boolean isMute) {
      mute = isMute;
   }

   public static boolean isMute() {
      return mute;
   }
}
