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

package by.stub.cli;

import org.eclipse.jetty.util.log.Logger;

/**
 * Class used to suppress default console output of Jetty
 *
 * @author Eric Mrak
 */
public final class EmptyLogger implements Logger {
   @Override
   public String getName() {
      return null;
   }

   @Override
   public void warn(final String s, final Object... objects) {
   }

   @Override
   public void warn(final Throwable throwable) {
   }

   @Override
   public void warn(final String s, final Throwable throwable) {
   }

   @Override
   public void info(final String s, final Object... objects) {
   }

   @Override
   public void info(final Throwable throwable) {
   }

   @Override
   public void info(final String s, final Throwable throwable) {
   }

   @Override
   public boolean isDebugEnabled() {
      return false;
   }

   @Override
   public void setDebugEnabled(final boolean b) {
   }

   @Override
   public void debug(final String s, final Object... objects) {
   }

   @Override
   public void debug(final Throwable throwable) {
   }

   @Override
   public void debug(final String s, final Throwable throwable) {
   }

   @Override
   public Logger getLogger(final String s) {
      return this;
   }

   @Override
   public void ignore(final Throwable throwable) {
   }
}
