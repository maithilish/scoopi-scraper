package org.codetab.scoopi.dao.fs;

import static java.util.Objects.isNull;

import java.net.URI;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.inject.Inject;

import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.helper.Fingerprints;

public class DocumentDao implements IDocumentDao {

    @Inject
    private FsHelper fsHelper;
    @Inject
    private Serializer serializer;
    @Inject
    private Factory factory;

    private final String fileName = "document.dat";

    @Override
    public Document get(final Fingerprint dir)
            throws DaoException, ChecksumException {
        URI uri = fsHelper.getURI(dir.getValue(), fileName);

        byte[] serializedData = fsHelper.readObjFile(uri);
        if (isNull(serializedData)) {
            return null;
        } else {
            Object obj = serializer.deserialize(serializedData);
            if (obj instanceof Document) {
                return (Document) obj;
            } else {
                throw new DaoException("object is not instance of Locator");
            }
        }
    }

    @Override
    public ZonedDateTime getDocumentDate(final Fingerprint dir)
            throws DaoException {
        URI uri = fsHelper.getURI(dir.getValue(), fileName);
        byte[] serializedData = fsHelper.readFile(uri, "/documentDate");
        if (isNull(serializedData)) {
            return null;
        } else {
            Object obj = serializer.deserialize(serializedData);
            if (obj instanceof ZonedDateTime) {
                return (ZonedDateTime) obj;
            } else {
                throw new DaoException(
                        "object is not instance of ZonedDateTime");
            }
        }
    }

    @Override
    public Fingerprint save(final Fingerprint dir, final Document document)
            throws DaoException {

        byte[] serializedData = serializer.serialize(document);
        byte[] documentDate = serializer.serialize(document.getFromDate());
        byte[] checksum = fsHelper.getChecksum(serializedData);

        Map<String, byte[]> dataMap = factory.createDataMap();
        dataMap.put("/data", serializedData);
        dataMap.put("/documentDate", documentDate);
        dataMap.put("/checksum", checksum);
        dataMap.put("/metadata",
                fsHelper.getMetadata(dir, document).getBytes());

        fsHelper.createDir(dir.getValue());
        URI uri = fsHelper.getURI(dir.getValue(), fileName);

        fsHelper.writeObjFile(uri, dataMap);

        return Fingerprints.fingerprint(serializedData);
    }

    @Override
    public void delete(final Fingerprint dir) throws DaoException {
        Path path = fsHelper.getDirPath(dir.getValue());
        fsHelper.deleteDir(path);
    }

}
