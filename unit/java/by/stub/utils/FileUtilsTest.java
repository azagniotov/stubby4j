package by.stub.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author: Alexander Zagniotov
 * Created: 4/21/13 2:17 PM
 */
public class FileUtilsTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();


   @Test
   public void shouldNotConvertFileToBytesWhenEmptyFilenameGiven() throws Exception {

      expectedException.expect(IOException.class);
      expectedException.expectMessage("Could not load file from path: bad/file/path");

      FileUtils.binaryFileToBytes(".", "bad/file/path");
   }
}
