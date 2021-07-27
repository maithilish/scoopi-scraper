package org.codetab.scoopi.dao.fs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codetab.scoopi.util.Util.dashit;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.ObjectFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataDaoTest {

    @InjectMocks
    private DataDao dataDao;

    @Mock
    private FsHelper fsHelper;
    @Mock
    private Serializer serializer;
    @Mock
    private Factory factory;

    private final String filePrefix = "data";

    private ObjectFactory objectFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objectFactory = new ObjectFactory();
    }

    @Test
    public void testGet() throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");
        URI uri = URI.create("testUri");

        Data data = objectFactory.createData("testDataDef");
        byte[] serializedData = SerializationUtils.serialize(data);

        when(fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()))).thenReturn(uri);
        when(fsHelper.readObjFile(uri)).thenReturn(null)
                .thenReturn(serializedData);
        when(serializer.deserialize(serializedData)).thenReturn(data);

        Data actual = dataDao.get(dir, file);
        assertThat(actual).isNull();

        Data expected = SerializationUtils.deserialize(serializedData);
        actual = dataDao.get(dir, file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testGetNotDataException()
            throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");
        URI uri = URI.create("testUri");

        Data data = objectFactory.createData("testDataDef");
        byte[] serializedData = SerializationUtils.serialize(data);

        when(fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()))).thenReturn(uri);
        when(fsHelper.readObjFile(uri)).thenReturn(serializedData);
        when(serializer.deserialize(serializedData))
                .thenReturn("not Data instance");

        assertThrows(DaoException.class, () -> dataDao.get(dir, file));
    }

    @Test
    public void testGetReadObjException()
            throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");
        URI uri = URI.create("testUri");

        when(fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()))).thenReturn(uri);
        when(fsHelper.readObjFile(uri))
                .thenThrow(new DaoException(new NullPointerException()));

        assertThrows(DaoException.class, () -> dataDao.get(dir, file));
    }

    @Test
    public void testGetReadObjNoFs() throws DaoException, ChecksumException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");
        URI uri = URI.create("testUri");

        when(fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()))).thenReturn(uri);
        when(fsHelper.readObjFile(uri))
                .thenThrow(new DaoException(new FileSystemNotFoundException()));

        assertThat(dataDao.get(dir, file)).isNull();
    }

    @Test
    public void testSave() throws DaoException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");
        URI uri = URI.create("testUri");

        Data data = objectFactory.createData("testDataDef");
        String checksum = "testchecksum";
        String metadata = "testmetadata";

        byte[] serializedData = SerializationUtils.serialize(data);
        byte[] checksumData = checksum.getBytes();
        byte[] metadataData = metadata.getBytes();

        HashMap<String, byte[]> dataMap = new HashMap<>();

        when(serializer.serialize(data)).thenReturn(serializedData);
        when(fsHelper.getChecksum(serializedData)).thenReturn(checksumData);
        when(factory.createDataMap()).thenReturn(dataMap);
        when(fsHelper.getMetadata(file, data)).thenReturn(metadata);
        when(fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()))).thenReturn(uri);

        dataDao.save(dir, file, data);

        assertThat(dataMap.get("/data")).isEqualTo(serializedData);
        assertThat(dataMap.get("/checksum")).isEqualTo(checksumData);
        assertThat(dataMap.get("/metadata")).isEqualTo(metadataData);

        verify(fsHelper).createDir(dir.getValue());
        verify(fsHelper).writeObjFile(uri, dataMap);
    }

    @Test
    public void testDelete() throws DaoException {
        Fingerprint dir = new Fingerprint("testdir");
        Fingerprint file = new Fingerprint("testFile");

        dataDao.delete(dir, file);

        verify(fsHelper).deleteFile(dir.getValue(),
                dashit(filePrefix, file.getValue()));
    }

}
