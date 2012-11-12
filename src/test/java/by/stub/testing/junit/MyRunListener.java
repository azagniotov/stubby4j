package by.stub.testing.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MyRunListener extends RunListener {

   private static final char ESCAPE = 27;
   private static final String RESET = String.format("%s[0m", ESCAPE);
   private static final String GREEN = String.format("%s[32m", ESCAPE);
   private static final String RED = String.format("%s[31m", ESCAPE);
   private static final String BLUE = String.format("%s[34m", ESCAPE);


   public MyRunListener() {

   }

   @Override
   public void testRunFinished(final Result result) throws Exception {
      final String status = String.format("\n\tTotal running time elapsed: %s sec",
            ((double) result.getRunTime() / 1000)
      );
      info(status);
   }

   @Override
   public void testStarted(final Description description) throws Exception {
      ok("\tExecuting: " + description.getMethodName());
   }

   @Override
   public void testFailure(final Failure failure) throws Exception {
      fail("\tFailed: " + failure.getDescription().getMethodName());
   }

   private void ok(final String msg) {
      print(GREEN, msg);
   }

   private void fail(final String msg) {
      print(RED, msg);
   }

   public static void info(final String msg) {
      print(BLUE, msg);
   }

   private static void print(final String color, final String msg) {
      System.out.println(String.format("%s%s%s", color, msg, RESET));
   }
}
