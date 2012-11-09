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

package by.stub.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;

/**
 * @author Alexander Zagniotov
 * @since 11/8/12, 8:30 AM
 */
@SuppressWarnings("serial")
public final class IOUtils {

   public static final String LINE_SEPARATOR_UNIX = "\n";
   public static final String LINE_SEPARATOR_MAC_OS_PRE_X = "\r";
   public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
   public static final String LINE_SEPARATOR_TOKEN = "[_T_O_K_E_N_]";
   public static final String LINE_SEPARATOR;

   static {
      final int initialSize = 4;
      final StringBuilderWriter stringBuilderWriter = new StringBuilderWriter(initialSize);
      final PrintWriter out = new PrintWriter(stringBuilderWriter);
      out.println();
      LINE_SEPARATOR = stringBuilderWriter.toString();
      out.close();
   }

   private IOUtils() {

   }

   public static String enforceSystemLineSeparator(final String loadedContent) {
      if (!StringUtils.isSet(loadedContent)) {
         return loadedContent;
      }

      return loadedContent
            .replace(LINE_SEPARATOR_WINDOWS, LINE_SEPARATOR_TOKEN)
            .replace(LINE_SEPARATOR_MAC_OS_PRE_X, LINE_SEPARATOR_TOKEN)
            .replace(LINE_SEPARATOR_UNIX, LINE_SEPARATOR_TOKEN)
            .replace(LINE_SEPARATOR_TOKEN, LINE_SEPARATOR);
   }

   private static final class StringBuilderWriter extends Writer implements Serializable {

      private final StringBuilder builder;

      private StringBuilderWriter(int capacity) {
         this.builder = new StringBuilder(capacity);
      }

      @Override
      public void write(char[] value, int offset, int length) {
         if (value != null) {
            builder.append(value, offset, length);
         }
      }

      @Override
      public void flush() throws IOException {
      }

      @Override
      public void close() throws IOException {
      }

      @Override
      public String toString() {
         return builder.toString();
      }
   }
}