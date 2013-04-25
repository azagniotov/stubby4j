package by.stub.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author: Alexander Zagniotov
 * Created: 4/21/13 2:17 PM
 */
public class FileUtilsTest {


   @Test
   public void shouldNotConvertFileToBytesWhenEmptyFilenameGiven() throws Exception {
      final byte[] actualBytes = FileUtils.binaryFileToBytes(".", "bad/file/path");

      assertThat(actualBytes).isEqualTo(new byte[]{});
   }
}
