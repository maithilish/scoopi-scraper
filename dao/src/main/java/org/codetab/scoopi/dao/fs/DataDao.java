package org.codetab.scoopi.dao.fs;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDataDao;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Fingerprint;

public class DataDao implements IDataDao {

    @Inject
    private FsHelper fsHelper;

    private final String filePrefix = "data";

    @Override
    public Data get(final Fingerprint dir, final Fingerprint file)
            throws DaoException, ChecksumException {
        URI uri = fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()));

        byte[] serializedData = null;
        try {
            serializedData = fsHelper.readDataObj(uri);
        } catch (DaoException e) {
            if (e.getCause() instanceof FileSystemNotFoundException) {
                // no data file, null is returned
            } else {
                throw e;
            }
        }

        if (isNull(serializedData)) {
            return null;
        } else {
            Object obj = SerializationUtils.deserialize(serializedData);
            if (obj instanceof Data) {
                return (Data) obj;
            } else {
                throw new DaoException("object is not instance of Data");
            }
        }
    }

    @Override
    public void save(final Fingerprint dir, final Fingerprint file,
            final Data data) throws DaoException {
        byte[] serializedData = SerializationUtils.serialize(data);
        byte[] checksum = fsHelper.getChecksum(serializedData);

        Map<String, byte[]> dataMap = new HashMap<>();
        dataMap.put("/data", serializedData);
        dataMap.put("/checksum", checksum);

        fsHelper.createDir(dir.getValue());
        URI uri = fsHelper.getURI(dir.getValue(),
                dashit(filePrefix, file.getValue()));

        fsHelper.writeObjFile(uri, dataMap);
    }

    @Override
    public void delete(final Fingerprint dir, final Fingerprint file)
            throws DaoException {
        fsHelper.deleteFile(dir.getValue(), file.getValue());
    }
}