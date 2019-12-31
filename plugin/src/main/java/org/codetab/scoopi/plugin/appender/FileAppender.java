package org.codetab.scoopi.plugin.appender;

import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.log.Log.CAT;
import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * File based appender. Writes output to file.
 * @author Maithilish
 *
 */
public final class FileAppender extends Appender {

    /**
     * logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FileAppender.class);

    @Inject
    private IOHelper ioHelper;
    @Inject
    private Configs configs;

    private PrintWriter writer;

    /**
     * <p>
     * private constructor.
     */
    @Inject
    private FileAppender() {
    }

    @Override
    public void init() {
        try {
            String path = getPluginField("file");
            if (configs.isCluster()) {
                final String fileName = FileUtils.getName(path);
                final String dir = FileUtils.getParent(path);
                path = dir + "/" + configs.getProperty("scoopi.cluster.nodeId")
                        + "-" + fileName;
            }
            writer = ioHelper.getPrintWriter(path);
            setInitialized(true);
            LOGGER.info("created {}, name: {}, file: {}",
                    this.getClass().getSimpleName(), getName(), path);
        } catch (IOException | DefNotFoundException e) {
            final String message =
                    spaceit("unable to create appender:", getName());
            errorLogger.log(CAT.ERROR, message, e);
        }
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
     */
    @Override
    public void run() {
        int count = 0;
        for (;;) {
            Object item = null;
            try {
                item = getQueue().take();
                count++;
                if (item == Marker.EOF) {
                    writer.flush();
                    break;
                }
                final String data = item.toString();
                writer.println(data);
            } catch (final InterruptedException e) {
                final String message = spaceit("appender:", getName());
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
        writer.close();
        LOGGER.info("appender: {}, {} item appended", getName(), count - 1);
    }

    /**
     * Append object to appender queue.
     * @param object
     *            object to append, not null
     * @throws InterruptedException
     *             if interrupted while queue put operation
     */
    @Override
    public void append(final Object object) throws InterruptedException {
        Validate.notNull(object, "object must not be null");
        if (isInitialized()) {
            getQueue().put(object);
        }
    }
}
