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

import org.eclipse.jetty.util.log.Logger;

public class EmptyLogger implements Logger {
   @Override
   public String getName() {
      return null;
   }

   @Override
   public void warn(String s, Object... objects) {
   }

   @Override
   public void warn(Throwable throwable) {
   }

   @Override
   public void warn(String s, Throwable throwable) {
   }

   @Override
   public void info(String s, Object... objects) {
   }

   @Override
   public void info(Throwable throwable) {
   }

   @Override
   public void info(String s, Throwable throwable) {
   }

   @Override
   public boolean isDebugEnabled() {
      return false;
   }

   @Override
   public void setDebugEnabled(boolean b) {
   }

   @Override
   public void debug(String s, Object... objects) {
   }

   @Override
   public void debug(Throwable throwable) {
   }

   @Override
   public void debug(String s, Throwable throwable) {
   }

   @Override
   public Logger getLogger(String s) {
      return this;
   }

   @Override
   public void ignore(Throwable throwable) {
   }
}
