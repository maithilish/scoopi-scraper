package org.codetab.scoopi.dao.fs;

import static java.util.Objects.isNull;

import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.dao.IMetadataDao;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.Metadata;
import org.codetab.scoopi.model.helper.Fingerprints;

public class MetadataDao implements IMetadataDao {

    @Inject
    private Helper helper;

    private final String fileName = "metadata.dat";

    @Override
    public Metadata get(final String id) throws DaoException {
        URI uri = helper.getDataFileURI(id, fileName);

        byte[] data = helper.readDataFile(uri);

        if (isNull(data)) {
            return null;
        } else {
            Object obj = SerializationUtils.deserialize(data);
            if (obj instanceof Metadata) {
                return (Metadata) obj;
            } else {
                throw new DaoException("object is not instance of Metadata");
            }
        }
    }

    @Override
    public Fingerprint save(final String id, final Metadata metadata)
            throws DaoException {
        helper.createDataDir(id);
        URI uri = helper.getDataFileURI(id, fileName);

        byte[] data = SerializationUtils.serialize(metadata);
        helper.createDataFile(uri, data);

        // return fingerprint locator with document
        return new Fingerprint(Fingerprints.fingerprint(data));
    }

    /**
     * Delete metadata file
     */
    @Override
    public void delete(final String id) throws DaoException {
        // TODO Auto-generated method stub

    }

    /**
     * Remove the containing folder
     */
    @Override
    public void remove(final String id) throws DaoException {
        Path path = helper.getDataDirPath(id);
        helper.removeDir(path);
    }

}
