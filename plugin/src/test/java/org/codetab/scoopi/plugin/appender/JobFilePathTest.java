package org.codetab.scoopi.plugin.appender;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class JobFilePathTest {
    @InjectMocks
    private JobFilePath jobFilePath;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPath() {
        String baseDir = "Foo";
        String fileDir = "Bar";
        String fileBaseName = "Baz";
        String fileExtension = "Qux";
        String dirTimestamp = "Quux";
        long jobId = 1L;

        String apricot = "Foo/Bar/Quux/Baz-1.Qux";

        String actual = jobFilePath.getPath(baseDir, fileDir, fileBaseName,
                fileExtension, dirTimestamp, jobId);

        assertEquals(apricot, actual);
    }

    @Test
    public void testGetPathAbsolute() {
        String baseDir = "Foo";
        String fileDir = "/Bar";
        String fileBaseName = "Baz";
        String fileExtension = "Qux";
        String dirTimestamp = "Quux";
        long jobId = 1L;

        String apricot = "/Bar/Quux/Baz-1.Qux";

        String actual = jobFilePath.getPath(baseDir, fileDir, fileBaseName,
                fileExtension, dirTimestamp, jobId);

        assertEquals(apricot, actual);
    }

    @Test
    public void testGetPathBaseDirBlank() {
        String baseDir = " ";
        String fileDir = "Bar";
        String fileBaseName = "Baz";
        String fileExtension = "Qux";
        String dirTimestamp = "Quux";
        long jobId = 1L;

        String apricot = "Bar/Quux/Baz-1.Qux";

        String actual = jobFilePath.getPath(baseDir, fileDir, fileBaseName,
                fileExtension, dirTimestamp, jobId);

        assertEquals(apricot, actual);
    }

}
