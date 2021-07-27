package org.codetab.scoopi.dao.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.model.helper.Fingerprints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DocumentDaoTest {

    @InjectMocks
    private DocumentDao documentDao;

    @Mock
    private FsHelper fsHelper;
    @Mock
    private Serializer serializer;
    @Mock
    private Factory factory;

    private final String fileName = "document.dat";

    private ObjectFactory objectFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectFactory = new ObjectFactory();
    }

    @Test
    public void testGet() throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        URI uri = URI.create("testUri");

        Document document = objectFactory.createDocument("foo",
                ZonedDateTime.now(), null, null, null);
        byte[] data = SerializationUtils.serialize(document);

        when(fsHelper.getURI(dir.getValue(), fileName)).thenReturn(uri);
        when(fsHelper.readObjFile(uri)).thenReturn(null).thenReturn(data);
        when(serializer.deserialize(data)).thenReturn(document);

        Document actual = documentDao.get(dir);
        assertThat(actual).isNull();

        Document expected = SerializationUtils.deserialize(data);
        actual = documentDao.get(dir);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetException() throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        URI uri = URI.create("testUri");

        Document document = objectFactory.createDocument("foo",
                ZonedDateTime.now(), null, null, null);
        byte[] data = SerializationUtils.serialize(document);

        when(fsHelper.getURI(dir.getValue(), fileName)).thenReturn(uri);
        when(fsHelper.readObjFile(uri)).thenReturn(data);
        when(serializer.deserialize(data)).thenReturn("not Document");

        SerializationUtils.deserialize(data);
        assertThrows(DaoException.class, () -> documentDao.get(dir));
    }

    @Test
    public void testGetDocumentDate() throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        URI uri = URI.create("testUri");

        ZonedDateTime documentDate = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(documentDate);

        when(fsHelper.getURI(dir.getValue(), fileName)).thenReturn(uri);
        when(fsHelper.readFile(uri, "/documentDate")).thenReturn(null)
                .thenReturn(data);
        when(serializer.deserialize(data)).thenReturn(documentDate);

        ZonedDateTime actual = documentDao.getDocumentDate(dir);
        assertThat(actual).isNull();

        ZonedDateTime expected = SerializationUtils.deserialize(data);
        actual = documentDao.getDocumentDate(dir);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetDocumentDateException()
            throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        URI uri = URI.create("testUri");

        ZonedDateTime documentDate = ZonedDateTime.now();
        byte[] data = SerializationUtils.serialize(documentDate);

        when(fsHelper.getURI(dir.getValue(), fileName)).thenReturn(uri);
        when(fsHelper.readFile(uri, "/documentDate")).thenReturn(data);
        when(serializer.deserialize(data)).thenReturn("not ZonedDateTime");

        SerializationUtils.deserialize(data);
        assertThrows(DaoException.class,
                () -> documentDao.getDocumentDate(dir));
    }

    @Test
    public void testSave() throws DaoException {
        Fingerprint dir = new Fingerprint("testdir");
        URI uri = URI.create("testUri");
        ZonedDateTime documentDate = ZonedDateTime.now();
        Document document = objectFactory.createDocument("foo", documentDate,
                null, null, null);
        String checksum = "testchecksum";
        String metadata = "testmetadata";
        byte[] data = SerializationUtils.serialize(document);
        byte[] documentDateData = SerializationUtils.serialize(documentDate);
        byte[] checksumData = checksum.getBytes();
        byte[] metadataData = metadata.getBytes();
        HashMap<String, byte[]> dataMap = new HashMap<>();

        when(serializer.serialize(document)).thenReturn(data);
        when(serializer.serialize(document.getFromDate()))
                .thenReturn(documentDateData);
        when(fsHelper.getChecksum(data)).thenReturn(checksumData);
        when(factory.createDataMap()).thenReturn(dataMap);
        when(fsHelper.getMetadata(dir, document)).thenReturn(metadata);
        when(fsHelper.getURI(dir.getValue(), fileName)).thenReturn(uri);

        Fingerprint actual = documentDao.save(dir, document);
        Fingerprint expected = Fingerprints.fingerprint(data);

        assertThat(actual).isEqualTo(expected);
        assertThat(dataMap.get("/data")).isEqualTo(data);
        assertThat(dataMap.get("/documentDate")).isEqualTo(documentDateData);
        assertThat(dataMap.get("/checksum")).isEqualTo(checksumData);
        assertThat(dataMap.get("/metadata")).isEqualTo(metadataData);

        verify(fsHelper).createDir(dir.getValue());
        verify(fsHelper).writeObjFile(uri, dataMap);
    }

    @Test
    public void testDelete() throws DaoException {
        Fingerprint dir = new Fingerprint("testdir");
        Path path = Paths.get("testpath");
        when(fsHelper.getDirPath(dir.getValue())).thenReturn(path);

        documentDao.delete(dir);

        verify(fsHelper).deleteDir(path);
    }
}
