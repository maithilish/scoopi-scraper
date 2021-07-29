package org.codetab.scoopi.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class AnalyzerConsoleTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    private ConsoleInterceptor interceptor;
    private AnalyzerConsole aConsole;

    @Before
    public void setUp() throws Exception {
        interceptor = new ConsoleInterceptor();
        aConsole = new AnalyzerConsole();
    }

    @Test
    public void testGetInputFileName() throws Exception {
        // user choice + file name
        InputStream in = IOUtils
                .toInputStream(String.format("%d%n%s", 2, "foo.txt"), "UTF-8");

        interceptor.capture(() -> {
            String choice = aConsole.getInput(in);
            assertThat(choice).isEqualTo("2");
        });
        String actual = (String) FieldUtils.readDeclaredField(aConsole,
                "fileName", true);
        assertThat(actual).isEqualTo("foo.txt");
    }

    @Test
    public void testGetInputLines() throws Exception {
        // user choice + no. of lines
        InputStream in = IOUtils.toInputStream(
                String.format("%d%n%d%n%s", 3, 25, "na"), "UTF-8");
        interceptor.capture(() -> {
            String choice = aConsole.getInput(in);
            assertThat(choice).isEqualTo("na");
        });
        int actual =
                (int) FieldUtils.readDeclaredField(aConsole, "lines", true);
        assertThat(actual).isEqualTo(25);
    }

    @Test
    public void testGetInputDefault() throws Exception {
        // user choice
        InputStream in = IOUtils.toInputStream(String.format("%d", 9), "UTF-8");

        interceptor.capture(() -> {
            String choice = aConsole.getInput(in);
            assertThat(choice).isEqualTo("9");
        });
    }

    @Test
    public void testDisplayPrompt() {
        Scanner scanner = new Scanner("dummy");
        String actual =
                interceptor.capture(() -> aConsole.displayPrompt(scanner));
        actual = new DigestUtils("SHA-1").digestAsHex(actual);
        assertThat(actual)
                .isEqualTo("e0ad8ec4d0da874431e747886672b4a3c3319d49");
    }

    @Test
    public void testShowPageSource() {
        String pageSource = "foo";
        String actual =
                interceptor.capture(() -> aConsole.showPageSource(pageSource));
        String nl = System.lineSeparator();
        assertThat(actual).isEqualTo("-----" + nl + "foo" + nl + "-----" + nl);
    }

    @Test
    public void testWritePageSource()
            throws IllegalAccessException, IOException {
        String fileName =
                System.getProperty("java.io.tmpdir") + "/page-source.txt";
        File file = new File(fileName);
        FieldUtils.writeDeclaredField(aConsole, "fileName", fileName, true);

        String pageSource = "foo";
        interceptor.capture(() -> aConsole.writePageSource(pageSource));

        String actual =
                FileUtils.readFileToString(file, Charset.defaultCharset());
        assertThat(actual).isEqualTo(pageSource);
        FileUtils.deleteQuietly(file);
    }

    @Test
    public void testWritePageSourceException()
            throws IllegalAccessException, IOException {
        String fileName = "/invalid.txt";
        FieldUtils.writeDeclaredField(aConsole, "fileName", fileName, true);

        String pageSource = "foo";
        interceptor.capture(() -> aConsole.writePageSource(pageSource));
    }

    @Test
    public void testShowElements() {
        ArrayList<String> elements = Lists.newArrayList("foo", "bar", "baz");
        String actual =
                interceptor.capture(() -> aConsole.showElements(elements));
        actual = new DigestUtils("SHA-1").digestAsHex(actual);
        assertThat(actual)
                .isEqualTo("16fe83a71a76615b4f3410e25d86660f38524b19");
    }

}
