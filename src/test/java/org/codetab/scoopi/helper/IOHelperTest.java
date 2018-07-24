package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class IOHelperTest {

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @InjectMocks
    private IOHelper ioHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetInputStream() throws IOException {
        String file = "/scoopi-default.xml";
        InputStream expected = IOHelper.class.getResourceAsStream(file);

        InputStream actual = ioHelper.getInputStream(file);

        assertThat(IOUtils.contentEquals(actual, expected)).isTrue();

        file = "java/lang/Object.class";
        expected = ClassLoader.getSystemResourceAsStream(file);

        actual = ioHelper.getInputStream(file);

        assertThat(IOUtils.contentEquals(actual, expected)).isTrue();
    }

    @Test
    public void testGetInputStreamShouldThrowException() throws IOException {
        // given
        testRule.expect(FileNotFoundException.class);

        // when
        ioHelper.getInputStream("xyz");
    }

    @Test
    public void testGetUrl() throws IOException {
        String file = "/scoopi-default.xml";
        URL expected = IOHelper.class.getResource(file);

        URL actual = ioHelper.getURL(file);

        assertThat(actual).isEqualTo(expected);

        file = "java/lang/Object.class";
        expected = ClassLoader.getSystemResource(file);

        actual = ioHelper.getURL(file);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetUrlShouldThrowException() throws IOException {
        // given
        testRule.expect(FileNotFoundException.class);

        // when
        ioHelper.getURL("xyz");
    }

    @Test
    public void testGetFile() throws IOException, URISyntaxException {
        String file = "/scoopi-default.xml";
        File expected = new File(IOHelper.class.getResource(file).toURI());

        File actual = ioHelper.getFile(file);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetFilesInDir() throws IOException, URISyntaxException {
        File tmpDir = FileUtils.getTempDirectory();
        File testDir = new File(tmpDir, "scoopi");
        File testChildDir = new File(testDir, "scoopi-child");
        File testFile1 = new File(testDir, "test1.txt");
        File testFile2 = new File(testDir, "test2.txt");
        File testFile3 = new File(testDir, "test3.xml");
        File testFile4 = new File(testChildDir, "test4.txt");

        FileUtils.forceMkdir(testChildDir);
        testFile1.createNewFile();
        testFile2.createNewFile();
        testFile3.createNewFile();
        testFile4.createNewFile();

        Collection<File> result = ioHelper.getFilesInDir(testDir.getPath(),
                new String[] {"txt"}, false);
        assertThat(result).contains(testFile1, testFile2);

        result = ioHelper.getFilesInDir(testDir.getPath(), new String[] {"xml"},
                false);
        assertThat(result).contains(testFile3);

        result = ioHelper.getFilesInDir(testDir.getPath(), new String[] {"txt"},
                true);
        assertThat(result).contains(testFile1, testFile2, testFile4);
        FileUtils.deleteQuietly(testDir);
    }

    @Test
    public void testGetFilesInDirInClasspath()
            throws IOException, URISyntaxException {
        File classDir =
                new File(new File("target/test-classes").getAbsolutePath());
        File testTemDir = new File(classDir, "test-tem");
        File testTemChildDir = new File(testTemDir, "scoopi-child");
        File testFile1 = new File(testTemDir, "test1.txt");
        File testFile2 = new File(testTemDir, "test2.txt");
        File testFile3 = new File(testTemDir, "test3.xml");
        File testFile4 = new File(testTemChildDir, "test4.txt");

        FileUtils.forceMkdir(testTemChildDir);
        testFile1.createNewFile();
        testFile2.createNewFile();
        testFile3.createNewFile();
        testFile4.createNewFile();

        Collection<File> result = ioHelper.getFilesInDir(testTemDir.getName(),
                new String[] {"txt"}, false);
        assertThat(result).contains(testFile1, testFile2);

        result = ioHelper.getFilesInDir(testTemDir.getPath(),
                new String[] {"xml"}, false);
        assertThat(result).contains(testFile3);

        result = ioHelper.getFilesInDir(testTemDir.getPath(),
                new String[] {"txt"}, true);
        assertThat(result).contains(testFile1, testFile2, testFile4);

        FileUtils.deleteQuietly(testTemDir);
    }

    @Test
    public void testGetPrintWriter() throws IOException {
        File tmpDir = FileUtils.getTempDirectory();
        File testDir = new File(tmpDir, "scoopi");
        File testFile1 = new File(testDir, "test1.txt");

        FileUtils.deleteQuietly(testDir);
        PrintWriter pw = ioHelper.getPrintWriter(testFile1.getAbsolutePath());
        pw.println("test");
        pw.close();

        String actual =
                FileUtils.readFileToString(testFile1, Charset.defaultCharset());

        assertThat(actual).isEqualTo("test" + System.lineSeparator());
        FileUtils.deleteQuietly(testDir);
    }

    @Test
    public void testGetFileShouldThrowException()
            throws IOException, URISyntaxException {
        // given
        testRule.expect(FileNotFoundException.class);

        // when
        ioHelper.getFile("xyz");
    }
}
