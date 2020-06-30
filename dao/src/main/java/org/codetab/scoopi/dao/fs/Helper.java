package org.codetab.scoopi.dao.fs;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codetab.scoopi.dao.DaoException;

public class Helper {

    public URI getDataFileURI(final String id, final String fileName) {
        // FIXME - dbfix, config it
        StringBuffer sb = new StringBuffer("jar:file:");
        sb.append("/orange/work/scoopifs/");
        sb.append(id);
        sb.append("/");
        sb.append(fileName);
        return URI.create(sb.toString());
    }

    public Path getDataFilePath(final String id, final String fileName) {
        // FIXME - dbfix, config it
        StringBuffer sb = new StringBuffer();
        sb.append("/orange/work/scoopifs/");
        sb.append(id);
        sb.append("/");
        sb.append(fileName);
        return Paths.get(sb.toString());
    }

    public Path getDataDirPath(final String id) {
        // FIXME - dbfix, config it
        StringBuffer sb = new StringBuffer();
        sb.append("/orange/work/scoopifs/");
        sb.append(id);
        return Paths.get(sb.toString());
    }

    public void createDataDir(final String id) throws DaoException {
        // FIXME - dbfix, config it
        try {
            Path path = Paths.get("/orange/work/scoopifs", id);
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new DaoException(e);
        }
    }

    public void createDataFile(final URI uri, final byte[] data)
            throws DaoException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Path path = fs.getPath("/data");
            try (OutputStream os = Files.newOutputStream(path, CREATE_NEW)) {
                os.write(data);
            }
        } catch (IOException | FileSystemNotFoundException e) {
            String message = spaceit("create data file", uri.toString());
            throw new DaoException(message, e);
        }
    }

    public byte[] readDataFile(final URI uri) throws DaoException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        byte[] data = null;
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Path path = fs.getPath("/data");
            try (InputStream is = Files.newInputStream(path, READ)) {
                data = IOUtils.toByteArray(is);
            }
        } catch (IOException | FileSystemNotFoundException e) {
            String message = spaceit("read data file", uri.toString());
            throw new DaoException(message, e);
        }
        return data;
    }

    public void deleteDataFile(final Path path) throws DaoException {
        try {
            Files.delete(path);
        } catch (IOException e) {
            String message = spaceit("delete data file", path.toString());
            throw new DaoException(message, e);
        }
    }

    public void removeDir(final Path path) throws DaoException {
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException | IllegalArgumentException e) {
            String message = spaceit("remove directory", path.toString());
            throw new DaoException(message, e);
        }
    }
}
