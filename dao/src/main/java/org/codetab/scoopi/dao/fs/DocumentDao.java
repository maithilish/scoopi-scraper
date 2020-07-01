package org.codetab.scoopi.dao.fs;

import static java.util.Objects.isNull;
import static org.codetab.scoopi.util.Util.dashit;

import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IDocumentDao;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.helper.Fingerprints;

public class DocumentDao implements IDocumentDao {

    @Inject
    private Helper helper;

    private final String filePrefix = "";

    @Override
    public Document get(final String dirName, final String fileName)
            throws DaoException {
        URI uri = helper.getDataFileURI(dirName, dashit(filePrefix, fileName));

        byte[] data = helper.readDataFile(uri);
        if (!fileName.equals(Fingerprints.fingerprint(data))) {
            // FIXME - dbfix, what to do if mismatch?
            throw new DaoException("fingerprint mismatch");
        }

        if (isNull(data)) {
            return null;
        } else {
            Object obj = SerializationUtils.deserialize(data);
            if (obj instanceof Document) {
                return (Document) obj;
            } else {
                throw new DaoException("object is not instance of Locator");
            }
        }
    }

    @Override
    public Fingerprint save(final String dirName, final Document document)
            throws DaoException {
        byte[] data = SerializationUtils.serialize(document);
        Fingerprint documentFp =
                new Fingerprint(Fingerprints.fingerprint(data));
        String fileName = documentFp.getValue();

        helper.createDataDir(dirName);
        URI uri = helper.getDataFileURI(dirName, dashit(filePrefix, fileName));

        helper.createDataFile(uri, data);

        // return fingerprint locator with document
        return documentFp;
    }

    @Override
    public void delete(final String dirName, final String fileName)
            throws DaoException {
        Path path =
                helper.getDataFilePath(dirName, dashit(filePrefix, fileName));
        helper.deleteDataFile(path);
    }
}
