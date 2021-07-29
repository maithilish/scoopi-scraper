package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IOHelperTest {

    private IOHelper ioHelper;

    private String testTmpDir =
            FileUtils.getTempDirectoryPath() + "/scoopitest";
    private String userDir = System.getProperty("user.dir");

    @Before
    public void setUp() throws Exception {
        ioHelper = new IOHelper();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(new File(testTmpDir));
    }

    @Test
    public void testGetInputStreamFromClasspath() throws IOException {
        String data = "foo bar baz";
        InputStream actual = ioHelper.getInputStream("/sample.txt");
        assertThat(IOUtils.readLines(actual, Charset.defaultCharset()).get(0))
                .isEqualTo(data);

        actual = ioHelper.getInputStream("sample.txt");
        assertThat(IOUtils.readLines(actual, Charset.defaultCharset()).get(0))
                .isEqualTo(data);
    }

    @Test
    public void testGetInputStreamFromFs() throws IOException {
        String data = "foo bar baz";
        String fileName = testTmpDir + "/foo.txt";
        FileUtils.writeStringToFile(new File(fileName), data,
                Charset.defaultCharset());

        InputStream actual = ioHelper.getInputStream(fileName);
        assertThat(IOUtils.readLines(actual, Charset.defaultCharset()).get(0))
                .isEqualTo(data);
    }

    @Test
    public void testGetReader() throws IOException {
        String data = "foo bar baz";
        Reader actual = ioHelper.getReader("/sample.txt");
        assertThat(IOUtils.readLines(actual).get(0)).isEqualTo(data);
    }

    @Test
    public void testGetURLFromClasspath()
            throws FileNotFoundException, MalformedURLException {
        String expected = String.format("file:%s%s", userDir,
                "/target/test-classes/sample.txt");

        URL actual = ioHelper.getURL("/sample.txt");
        assertThat(actual.toString()).isEqualTo(expected);

        actual = ioHelper.getURL("sample.txt");
        assertThat(actual.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetURLFromFs() throws IOException {
        String fileName = testTmpDir + "/sample.txt";
        File file = new File(fileName);
        FileUtils.touch(file);

        String expected = String.format("file:%s%s", testTmpDir, "/sample.txt");

        URL actual = ioHelper.getURL(fileName);

        assertThat(actual.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetURLFromFsException() throws FileNotFoundException {
        String fileName = "/xyz.txt";
        assertThrows(FileNotFoundException.class,
                () -> ioHelper.getURL(fileName));
    }

    @Test
    public void testGetFile() throws IOException, URISyntaxException {
        String data = "foo bar baz";
        File actual = ioHelper.getFile("/sample.txt");
        assertThat(FileUtils.readLines(actual, Charset.defaultCharset()).get(0))
                .isEqualTo(data);
    }

    @Test
    public void testGetResourceURL() {
        String expected = String.format("file:%s%s", userDir,
                "/target/test-classes/sample.txt");

        URL actual = ioHelper.getResourceURL("/sample.txt");
        assertThat(actual.toString()).isEqualTo(expected);

        actual = ioHelper.getResourceURL("sample.txt");
        assertThat(actual.toString()).isEqualTo(expected);
    }

    @Test
    public void testGetFilesInDirFromClasspath()
            throws URISyntaxException, IOException {
        String dir = String.format("%s%s", userDir, "/target/test-classes");

        List<String> expected = new ArrayList<>();
        expected.add(dir + "/sample.txt");

        Collection<String> actual =
                ioHelper.getFilesInDir("/", Arrays.array("txt"));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    public void testGetFilesInDirFromJar()
            throws URISyntaxException, IOException {

        List<String> expected = new ArrayList<>();
        expected.add("/job.yml");
        expected.add("/ex-99/pl.yml");
        expected.add("/ex-99/bs.yml");
        expected.add("/ex-99/snapshot.yml");

        String jarDir = String.format("%s%s%s", "jar:file:", userDir,
                "/src/test/resources/testdefs.jar!/");

        Collection<String> actual =
                ioHelper.getFilesInDir(jarDir, Arrays.array("yml"));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    public void testGetFilesInDirFromFs()
            throws URISyntaxException, IOException {
        String fileName = testTmpDir + "/sample.txt";
        File file = new File(fileName);
        FileUtils.touch(file);

        List<String> expected = new ArrayList<>();
        expected.add(fileName);

        Collection<String> actual =
                ioHelper.getFilesInDir(testTmpDir, Arrays.array("txt"));

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    public void testGetPrintWriter() throws IOException {
        String fileName = testTmpDir + "/scoopitest/sample.txt";

        ioHelper.getPrintWriter(fileName);
        assertThat(Files.exists(Paths.get(fileName))).isTrue();
    }

    // @Test
    // public void test() throws IOException, URISyntaxException {
    // String dir = String.format("%s%s%s", "jar:file:", userDir,
    // "/src/test/resources/testdefs.jar!/ex-99/bs.yml");
    // URL dirURL = new URL(dir);
    // URI uri = dirURL.toURI();
    // FileSystem jarFs = FileSystems.newFileSystem(uri,
    // Collections.<String, Object>emptyMap());
    // Path p = Paths.get(dir);
    // System.out.println(p.getFileName());
    // Path dirPath = jarFs.getPath(p.getFileName().toString());
    // System.out.println(Files.exists(dirPath));
    // System.out.println(dirPath.getFileSystem());
    //
    // }

}
