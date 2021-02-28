package io.github.azagniotov.stubby4j.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class FileUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotConvertFileToBytesWhenBadFilenameGiven() throws Exception {

        expectedException.expect(IOException.class);
        expectedException.expectMessage("Could not load file from path: bad/file/path");

        FileUtils.binaryFileToBytes(".", "bad/file/path");
    }

    @Test
    public void shouldNotLoadFileFromURWhenBadFilenameGiven() throws Exception {

        expectedException.expect(IOException.class);
        expectedException.expectMessage("Could not load file from path: bad/file/path");

        FileUtils.uriToFile("bad/file/path");
    }
}
