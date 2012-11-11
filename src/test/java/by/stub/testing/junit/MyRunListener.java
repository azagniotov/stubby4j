package by.stub.testing.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public class MyRunListener extends RunListener {

   private static final char ESCAPE = 27;
   private static final String RESET = String.format("%s[0m", ESCAPE);
   private static final String GREEN = String.format("%s[32m", ESCAPE);

   public MyRunListener() {

   }

   @Override
   public void testStarted(final Description description) throws Exception {
      ok("\t" + description.getMethodName());
   }

   private void ok(final String msg) {
      print(GREEN, msg);
   }

   private static void print(final String color, final String msg) {
      System.out.println(String.format("%s%s%s", color, msg, RESET));
   }
}
