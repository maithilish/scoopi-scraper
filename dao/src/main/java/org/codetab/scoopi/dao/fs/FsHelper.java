package org.codetab.scoopi.dao.fs;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.dao.ChecksumException;
import org.codetab.scoopi.dao.DaoException;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Fingerprint;
import org.codetab.scoopi.model.helper.Fingerprints;
import org.codetab.scoopi.util.Util;

public class FsHelper {

    @Inject
    private Configs configs;
    @Inject
    private Serializer serializer;

    /**
     * Write multiple files contained in input map to a filesystem.
     * <p>
     * The map key is file path and value is content.
     *
     * @param uri
     *            - URI of filesystem
     * @param files
     *            - map of file path and content
     * @throws DaoException
     */
    public void writeObjFile(final URI uri, final Map<String, byte[]> files)
            throws DaoException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            for (String pathKey : files.keySet()) {
                Path path = fs.getPath(pathKey);
                try (OutputStream os =
                        Files.newOutputStream(path, CREATE_NEW)) {
                    os.write(files.get(pathKey));
                }
            }
        } catch (IOException | FileSystemNotFoundException e) {
            String message = spaceit("create data file", uri.toString());
            throw new DaoException(message, e);
        }
    }

    /**
     * Get data bytes from a filesystem.
     * @param uri
     *            - URI of filesystem
     * @return byte array
     * @throws DaoException
     * @throws ChecksumException
     */
    public byte[] readObjFile(final URI uri)
            throws DaoException, ChecksumException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        byte[] data = null;
        byte[] checksum = null;
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Path path = fs.getPath("/data");
            try (InputStream is = Files.newInputStream(path, READ)) {
                data = IOUtils.toByteArray(is);
            }
            path = fs.getPath("/checksum");
            try (InputStream is = Files.newInputStream(path, READ)) {
                checksum = IOUtils.toByteArray(is);
            }
            Fingerprint dataFp = Fingerprints.fingerprint(data);
            Fingerprint checksumFp = serializer.deserialize(checksum);
            if (!dataFp.equals(checksumFp)) {
                String message = spaceit("checksum mismatch", uri.toString());
                throw new ChecksumException(message);
            }
        } catch (UnsupportedOperationException e) {
            throw new ChecksumException(e);
        } catch (IOException | FileSystemNotFoundException e) {
            String message = spaceit("read data file", uri.toString());
            throw new DaoException(message, e);
        }
        return data;
    }

    /**
     * Get bytes of a file path from a filesystem.
     *
     * @param uri
     *            - URI of filesystem
     * @param file
     *            - path of file to read
     * @return byte array
     * @throws DaoException
     */
    public byte[] readFile(final URI uri, final String file)
            throws DaoException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        byte[] bytes = null;
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Path path = fs.getPath(file);
            try (InputStream is = Files.newInputStream(path, READ)) {
                bytes = IOUtils.toByteArray(is);
            }
            return bytes;
        } catch (IOException | FileSystemNotFoundException
                | UnsupportedOperationException e) {
            String message = spaceit("read file path", uri.toString());
            throw new DaoException(message, e);
        }
    }

    public void deleteDir(final Path path) throws DaoException {
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException | IllegalArgumentException e) {
            String message = spaceit("remove directory", path.toString());
            throw new DaoException(message, e);
        }
    }

    public void deleteFile(final String dir, final String file)
            throws DaoException {
        Path path = getFilePath(dir, file);
        try {
            Files.delete(path);
        } catch (IOException e) {
            String message = spaceit("delete file", path.toString());
            throw new DaoException(message, e);
        }
    }

    public byte[] getChecksum(final byte[] data) {
        Fingerprint fp = Fingerprints.fingerprint(data);
        return serializer.serialize(fp);
    }

    public URI getURI(final String dir, final String file) {
        String type = configs.getConfig("scoopi.datastore.type", "jar:file:");
        String dataStorePath = getDataStorePath();
        Path path = Paths.get(type, dataStorePath, dir, file);
        return URI.create(path.toString());
    }

    public Path getFilePath(final String dir, final String file) {
        String dataStorePath = getDataStorePath();
        return Paths.get(dataStorePath, dir, file);
    }

    public Path getDirPath(final String dir) {
        String dataStorePath = getDataStorePath();
        return Paths.get(dataStorePath, dir);
    }

    public void createDir(final String dir) throws DaoException {
        try {
            String dataStorePath = getDataStorePath();
            Path path = Paths.get(dataStorePath, dir);
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

    /**
     * Get absolute path of configured data folder path
     * @return
     */
    private String getDataStorePath() {
        return Paths.get(configs.getConfig("scoopi.datastore.path", "data"))
                .toAbsolutePath().toString();
    }

    public void writeMetaFile(final Fingerprint dir, final String data)
            throws DaoException {
        String dataStorePath = getDataStorePath();
        File file = Paths.get(dataStorePath, dir.getValue(), "metadata.txt")
                .toFile();
        try {
            FileUtils.writeStringToFile(file, data, Charset.defaultCharset(),
                    true);
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

    public String getMetadata(final Fingerprint fp, final Document document) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        Util.append(sb, "Type: ", "document", nl);
        Util.append(sb, "Name: ", document.getName(), nl);
        Util.append(sb, "Group: ", document.getGroup(), nl);
        Util.append(sb, "URL: ", document.getUrl(), nl);
        Util.append(sb, "From Date: ", document.getFromDate().toString(), nl);
        Util.append(sb, "Fingerprint: ", fp.getValue(), nl);
        Util.append(sb, "---", nl);
        return sb.toString();
    }

    public String getMetadata(final Fingerprint fp, final Data data) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        Util.append(sb, "Type: ", "data", nl);
        Util.append(sb, "Name: ", data.getName(), nl);
        Util.append(sb, "DataDef: ", data.getDataDef(), nl);
        Util.append(sb, "Run Date: ", data.getRunDate().toString(), nl);
        Util.append(sb, "Fingerprint: ", fp.getValue(), nl);
        Util.append(sb, "---", nl);
        return sb.toString();
    }
}
