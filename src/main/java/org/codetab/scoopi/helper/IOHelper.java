package org.codetab.scoopi.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(IOHelper.class);

    public InputStream getInputStream(final String fileName)
            throws FileNotFoundException {
        InputStream stream = IOHelper.class.getResourceAsStream(fileName);
        if (stream == null) {
            stream = ClassLoader.getSystemResourceAsStream(fileName);
            if (stream == null) {
                throw new FileNotFoundException(fileName);
            }
        }
        return stream;
    }

    public URL getURL(final String fileName) throws FileNotFoundException {
        URL url = IOHelper.class.getResource(fileName);
        if (url == null) {
            url = ClassLoader.getSystemResource(fileName);
            if (url == null) {
                throw new FileNotFoundException(fileName);
            }
        }
        return url;
    }

    public StreamSource getStreamSource(final String fileName)
            throws FileNotFoundException {
        InputStream is = getInputStream(fileName);
        return new StreamSource(is);
    }

    public File getFile(final String fileName)
            throws FileNotFoundException, URISyntaxException {
        URL url = getURL(fileName);
        return new File(url.toURI());
    }

    public Collection<File> getFilesInDir(final String dir,
            final String[] extensions, final boolean recursive)
            throws IOException, URISyntaxException {

        URL dirURL = IOHelper.class.getClassLoader().getResource(dir);
        Collection<File> files;
        if (dirURL == null) {
            files = FileUtils.listFiles(new File(dir), extensions, recursive);
        } else {
            files = FileUtils.listFiles(new File(dirURL.toURI()), extensions,
                    recursive);
        }
        return files;
    }

    /**
     * Creates parent directory if not exists and returns print writer.
     * @param fileName
     *            file name
     * @return PrintWriter
     * @throws IOException
     */
    public PrintWriter getPrintWriter(final String fileName)
            throws IOException {
        File file = new File(fileName);
        FileUtils.forceMkdirParent(file);
        return new PrintWriter(file);
    }
}
