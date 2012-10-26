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
