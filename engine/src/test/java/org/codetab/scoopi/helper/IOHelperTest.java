package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
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

        String testDir =
                FileUtils.getTempDirectory().getAbsolutePath() + "/scoopi";
        file = createTmpTestFile(testDir + "/test1.txt", "test1")
                .getAbsolutePath();

        actual = ioHelper.getInputStream(file);

        expected = new FileInputStream(file);
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

        String testDir =
                FileUtils.getTempDirectory().getAbsolutePath() + "/scoopi";
        file = createTmpTestFile(testDir + "/test1.txt", "test1")
                .getAbsolutePath();

        actual = ioHelper.getURL(file);

        assertThat(actual).isEqualTo(new File(file).toURI().toURL());
    }

    @Test
    public void testGetUrlShouldThrowException() throws IOException {
        try {
            ioHelper.getURL("xyz");
            fail("should throw FileNotFoundException");
        } catch (FileNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("xyz");
        }

        testRule.expect(FileNotFoundException.class);
        ioHelper.getURL("httpx:///xyz");
    }

    @Test
    public void testGetResourceUrl() throws IOException {
        String file = "/scoopi-default.xml";
        URL expected = IOHelper.class.getResource(file);

        URL actual = ioHelper.getResourceURL(file);

        assertThat(actual).isEqualTo(expected);

        file = "java/lang/Object.class";
        expected = ClassLoader.getSystemResource(file);

        actual = ioHelper.getResourceURL(file);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetFile() throws IOException, URISyntaxException {
        String file = "/jdoconfig.properties";
        File expected = new File(IOHelper.class.getResource(file).toURI());

        File actual = ioHelper.getFile(file);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetFilesInDirFromFs()
            throws IOException, URISyntaxException {
        String testDir =
                FileUtils.getTempDirectory().getAbsolutePath() + "/scoopi";
        File testFile1 = createTmpTestFile(testDir + "/test1.txt", "test1");
        File testFile2 = createTmpTestFile(testDir + "/test2.txt", "test2");
        File testFile3 = createTmpTestFile(testDir + "/test3.xml", "test3");
        File testFile4 =
                createTmpTestFile(testDir + "/child/test4.txt", "test4");

        Collection<String> result =
                ioHelper.getFilesInDir(testDir, new String[] {"txt"});
        assertThat(result).contains(testFile1.getAbsolutePath(),
                testFile2.getAbsolutePath());

        result = ioHelper.getFilesInDir(testDir, new String[] {"xml"});
        assertThat(result).contains(testFile3.getAbsolutePath());

        result = ioHelper.getFilesInDir(testDir, new String[] {"txt"});
        assertThat(result).contains(testFile1.getAbsolutePath(),
                testFile2.getAbsolutePath(), testFile4.getAbsolutePath());
        FileUtils.deleteQuietly(new File(testDir));
    }

    @Test
    public void testGetFilesInDirFromClasspath()
            throws IOException, URISyntaxException {
        // create test files in build dir
        String testDir = "target/test-classes/test-tem";
        File testFile1 = createTmpTestFile(testDir + "/test1.txt", "test1");
        File testFile2 = createTmpTestFile(testDir + "/test2.txt", "test2");
        File testFile3 = createTmpTestFile(testDir + "/test3.xml", "test3");
        File testFile4 =
                createTmpTestFile(testDir + "/child/test4.txt", "test4");

        // access through classpath
        Collection<String> result =
                ioHelper.getFilesInDir("/test-tem", new String[] {"txt"});
        assertThat(result).contains(testFile1.getAbsolutePath(),
                testFile2.getAbsolutePath());

        result = ioHelper.getFilesInDir("/test-tem", new String[] {"xml"});
        assertThat(result).contains(testFile3.getAbsolutePath());

        result = ioHelper.getFilesInDir("/test-tem", new String[] {"txt"});
        assertThat(result).contains(testFile1.getAbsolutePath(),
                testFile2.getAbsolutePath(), testFile4.getAbsolutePath());

        FileUtils.deleteQuietly(new File(testDir));
    }

    // this is tested in IT, here only for coverage
    @Test
    public void testGetFilesInDirFromJar()
            throws IOException, URISyntaxException {
        ioHelper.getFilesInDir("/java/lang/Object.class",
                new String[] {"class"});
    }

    @Test
    public void testGetPrintWriter() throws IOException {
        String testDir = FileUtils.getTempDirectory() + "/scoopi";
        File testFile = createTmpTestFile(testDir + "/test1.txt", "test1");

        FileUtils.deleteQuietly(testFile);
        PrintWriter pw = ioHelper.getPrintWriter(testFile.getAbsolutePath());
        pw.println("test");
        pw.close();

        String actual =
                FileUtils.readFileToString(testFile, Charset.defaultCharset());

        assertThat(actual).isEqualTo("test" + System.lineSeparator());
        FileUtils.deleteQuietly(testFile);
    }

    @Test
    public void testGetFileShouldThrowException()
            throws IOException, URISyntaxException {
        // given
        testRule.expect(FileNotFoundException.class);

        // when
        ioHelper.getFile("xyz");
    }

    private File createTmpTestFile(final String file, final String data)
            throws IOException {
        File testFile = new File(file);
        FileUtils.forceMkdirParent(testFile);
        testFile.createNewFile();
        Charset c = null;
        FileUtils.writeStringToFile(testFile, data, c);
        return testFile;
    }

}
