package org.codetab.scoopi.dao.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.Fingerprints;
import org.codetab.scoopi.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FsHelperTest {

    @InjectMocks
    private FsHelper fsHelper;

    @Mock
    private Configs configs;
    @Mock
    private Serializer serializer;

    private String testTmpDir = FileUtils.getTempDirectoryPath() + "/scooptest";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(new File(testTmpDir));
    }

    @Test
    public void testWriteObjFile()
            throws DaoException, ChecksumException, IOException {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        Map<String, byte[]> dataMap = new HashMap<>();
        dataMap.put("/data", data);
        dataMap.put("/checksum", checksum);

        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/");
        URI uri = fsHelper.getURI(testTmpDir, "foo.doc");

        FileUtils.forceMkdir(new File(testTmpDir));
        fsHelper.writeObjFile(uri, dataMap);

        when(serializer.deserialize(checksum)).thenReturn(fingerprint);
        fsHelper.readObjFile(uri);
    }

    @Test
    public void testWriteObjFileException()
            throws DaoException, URISyntaxException {
        URI uri = new URI("jar:file:/invalidpath/daotest.jar");
        Map<String, byte[]> dataMap = new HashMap<>();

        assertThrows(DaoException.class,
                () -> fsHelper.writeObjFile(uri, dataMap));
    }

    @Test
    public void testReadObjFileChecksumException()
            throws DaoException, ChecksumException, IOException {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        URI uri = writeFileObj(data, checksum);

        Fingerprint badFingerprint = new Fingerprint("baz");
        when(serializer.deserialize(checksum)).thenReturn(badFingerprint);

        assertThrows(ChecksumException.class, () -> fsHelper.readObjFile(uri));
    }

    @Test
    public void testReadObjFileException()
            throws DaoException, URISyntaxException {
        URI uri = new URI("jar:file:/invalidpath/daotest.jar");

        assertThrows(DaoException.class, () -> fsHelper.readObjFile(uri));
    }

    @Test
    public void testReadObjFileOpException()
            throws DaoException, ChecksumException, IOException {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        URI uri = writeFileObj(data, checksum);

        when(serializer.deserialize(checksum))
                .thenThrow(UnsupportedOperationException.class);

        assertThrows(ChecksumException.class, () -> fsHelper.readObjFile(uri));
    }

    @Test
    public void testReadFile() throws DaoException, IOException {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        URI uri = writeFileObj(data, checksum);

        byte[] actual = fsHelper.readFile(uri, "/checksum");

        assertThat(actual).isEqualTo(checksum);
    }

    @Test
    public void testReadFileException() throws DaoException, IOException {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        URI uri = writeFileObj(data, checksum);

        assertThrows(DaoException.class,
                () -> fsHelper.readFile(uri, "invalid file"));
    }

    @Test
    public void testDeleteDir() throws IOException, DaoException {
        Path dir = Paths.get(testTmpDir, "foo");
        FileUtils.deleteQuietly(dir.toFile());
        FileUtils.forceMkdir(dir.toFile());
        assertThat(Files.exists(dir)).isTrue();

        fsHelper.deleteDir(dir);
        assertThat(Files.exists(dir)).isFalse();
    }

    @Test
    public void testDeleteDirException() throws IOException, DaoException {
        Path file = Paths.get(testTmpDir, "foo");
        FileUtils.deleteQuietly(file.toFile());
        FileUtils.touch(file.toFile());

        assertThrows(DaoException.class, () -> fsHelper.deleteDir(file));
    }

    @Test
    public void testDeleteFile() throws IOException, DaoException {
        Path file = Paths.get(testTmpDir, "foo.doc");
        FileUtils.deleteQuietly(file.toFile());
        FileUtils.touch(file.toFile());

        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/");

        assertThat(Files.exists(file)).isTrue();
        fsHelper.deleteFile(testTmpDir, "foo.doc");
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    public void testDeleteFileException() throws IOException, DaoException {
        Path file = Paths.get(testTmpDir, "foo.doc");
        FileUtils.deleteQuietly(file.toFile());

        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/");

        assertThrows(DaoException.class,
                () -> fsHelper.deleteFile(testTmpDir, "foo.doc"));
    }

    @Test
    public void testGetChecksum() {
        ZonedDateTime date = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(date);
        Fingerprint fingerprint = Fingerprints.fingerprint(data);
        byte[] checksum = SerializationUtils.serialize(fingerprint);

        when(serializer.serialize(fingerprint)).thenReturn(checksum);

        assertThat(fsHelper.getChecksum(data)).isEqualTo(checksum);
    }

    @Test
    public void testGetURI() {
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("zip:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/cache");

        URI actual = fsHelper.getURI("foo", "bar.txt");

        assertThat(actual.toString()).isEqualTo("zip:file:/cache/foo/bar.txt");
    }

    @Test
    public void testGetFilePath() {
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/cache");

        Path actual = fsHelper.getFilePath("foo", "bar.txt");

        assertThat(actual.toString()).isEqualTo("/cache/foo/bar.txt");
    }

    @Test
    public void testGetDirPath() {
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/cache");

        Path actual = fsHelper.getDirPath("foo");

        assertThat(actual.toString()).isEqualTo("/cache/foo");
    }

    @Test
    public void testCreateDir() throws DaoException {
        Path dir = Paths.get(testTmpDir, "foo");
        FileUtils.deleteQuietly(dir.toFile());

        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn(testTmpDir);

        fsHelper.createDir("foo");

        assertThat(Files.exists(dir)).isTrue();
    }

    @Test
    public void testCreateDirException() throws DaoException {
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/invalidpath");

        assertThrows(DaoException.class, () -> fsHelper.createDir("foo"));
    }

    @Test
    public void testWriteMetaFile() throws DaoException, IOException {
        String metadata = "test metadata";
        File file = Paths.get(testTmpDir, "foo", "metadata.txt").toFile();
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn(testTmpDir);

        Fingerprint fingerprint = new Fingerprint("foo");
        fsHelper.writeMetaFile(fingerprint, metadata);

        String actual =
                FileUtils.readFileToString(file, Charset.defaultCharset());

        assertThat(actual).isEqualTo(metadata);
    }

    @Test
    public void testWriteMetaFileException() throws DaoException, IOException {
        String metadata = "test metadata";
        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/invalidpath");

        Fingerprint fingerprint = new Fingerprint("foo");

        assertThrows(DaoException.class,
                () -> fsHelper.writeMetaFile(fingerprint, metadata));
    }

    @Test
    public void testGetDocumentMetadata() {
        ZonedDateTime docDate = ZonedDateTime.of(2021, 12, 30, 10, 45, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        ObjectFactory of = new ObjectFactory();
        Fingerprint fp = new Fingerprint("tFingerprint");
        Document document = of.createDocument("tName", docDate, "tUrl",
                "tLocatorGroup", fp);

        String n = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        Util.append(sb, "Type: document", n);
        Util.append(sb, "Name: tName", n);
        Util.append(sb, "Group: tLocatorGroup", n);
        Util.append(sb, "URL: tUrl", n);
        Util.append(sb, "From Date: 2021-12-30T10:45:30+05:30[Asia/Kolkata]",
                n);
        Util.append(sb, "Fingerprint: tFingerprint", n);
        Util.append(sb, "---", n);
        String expected = sb.toString();

        String actual = fsHelper.getMetadata(fp, document);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetDataMetadata() {
        ZonedDateTime runDate = ZonedDateTime.of(2021, 12, 30, 10, 45, 30, 0,
                ZoneId.of("Asia/Kolkata"));
        ObjectFactory of = new ObjectFactory();
        Fingerprint fp = new Fingerprint("tFingerprint");
        Data data = of.createData("tDataDef");
        data.setName("tName");
        data.setRunDate(runDate);

        String n = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        Util.append(sb, "Type: data", n);
        Util.append(sb, "Name: tName", n);
        Util.append(sb, "DataDef: tDataDef", n);
        Util.append(sb, "Run Date: 2021-12-30T10:45:30+05:30[Asia/Kolkata]", n);
        Util.append(sb, "Fingerprint: tFingerprint", n);
        Util.append(sb, "---", n);
        String expected = sb.toString();

        String actual = fsHelper.getMetadata(fp, data);
        assertThat(actual).isEqualTo(expected);
    }

    private URI writeFileObj(final byte[] data, final byte[] checksum)
            throws DaoException, IOException {
        Map<String, byte[]> dataMap = new HashMap<>();
        dataMap.put("/data", data);
        dataMap.put("/checksum", checksum);

        when(configs.getConfig("scoopi.datastore.type", "jar:file:"))
                .thenReturn("jar:file:");
        when(configs.getConfig("scoopi.datastore.path", "data"))
                .thenReturn("/");
        URI uri = fsHelper.getURI(testTmpDir, "foo.doc");

        FileUtils.forceMkdir(new File(testTmpDir));
        fsHelper.writeObjFile(uri, dataMap);

        return uri;
    }

}
