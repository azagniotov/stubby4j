package by.stub.utils;

import by.stub.cli.CommandLineInterpreter;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

/**
 * @author: Alexander Zagniotov
 * Created: 4/21/13 2:17 PM
 */
public class FileUtilsTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   @BeforeClass
   public static void beforeClass() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void shouldNotConvertFileToBytesWhenEmptyFilenameGiven() throws Exception {

      expectedException.expect(IOException.class);
      expectedException.expectMessage("Could not load file from path: bad/file/path");

      FileUtils.binaryFileToBytes("bad/file/path");
   }
}
