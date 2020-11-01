package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.PrintPayload;

/**
 * <p>
 * File based appender. Writes output to file.
 * @author Maithilish
 *
 */
public final class FileAppender extends Appender {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private IOHelper ioHelper;
    @Inject
    private Configs configs;

    private String baseDir;
    private String fileDir;
    private String fileBaseName;
    private String fileExtension;

    private String dirTimestamp;

    @Inject
    private FileAppender() {
    }

    @Override
    public void init() {
        try {
            String filePath = getPluginField("file");
            baseDir = configs.getConfig("scoopi.appender.file.baseDir", "");
            fileDir = FilenameUtils.getFullPath(filePath);
            fileBaseName = FilenameUtils.getBaseName(filePath);
            fileExtension = FilenameUtils.getExtension(filePath);
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(configs.getConfig(
                            "outputDirTimestampPattern", "yyyyMMMdd-HHmmss"));
            dirTimestamp = configs.getRunDateTime().format(formatter);
            setInitialized(true);
        } catch (DefNotFoundException e) {
            errors.inc();
            LOG.error("unable to create appender: {} [{}]", getName(),
                    ERROR.DATAERROR, e);
        }
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
     */
    @Override
    public void run() {
        int count = 0;
        // int jobCount = 0;
        // Random random = new Random();

        for (;;) {
            PrintPayload printPayload = null;
            try {
                printPayload = getQueue().take();
                if (printPayload.getData() == Marker.END_OF_STREAM) {
                    break;
                }
            } catch (final InterruptedException e) {
                errors.inc();
                LOG.error("appender: {} [{}]", getName(), ERROR.INTERNAL, e);
                Thread.currentThread().interrupt();
            }
            if (nonNull(printPayload)) {
                String jobFilePath = getJobFilePath(printPayload);
                try (PrintWriter writer =
                        ioHelper.getPrintWriter(jobFilePath)) {

                    // int r = (jobCount++) % (random.nextInt(10) + 2);
                    // if (r == 0) {
                    // throw new IOException("force error");
                    // }

                    Object data = printPayload.getData();
                    if (data instanceof List) {
                        List<? extends Object> list = (List<?>) data;
                        for (Object o : list) {
                            writer.println(o.toString());
                            count++;
                        }
                    } else {
                        writer.println(data);
                        count++;
                    }
                    printPayload.setProcessed(true);
                } catch (IOException e) {
                    printPayload.setProcessed(false);
                    // recoverable - so no data error
                    LOG.error("appender: {} file path: {} [{}]", getName(),
                            jobFilePath, ERROR.ERROR);
                }
                printPayload.finished();
            }
        }
        LOG.info("appender: {}, {} item appended", getName(), count);
    }

    /**
     * Construct path defined in plugin.
     * <p>
     * If path (plugin) is not absolute then prefix it with path, if not blank,
     * defined by config: scoopi.appender.file.baseDir
     * <p>
     * Example: scoopi.appender.file.baseDir=/tmp/scoopi for plugin file path:
     * output, appender writes file to /tmp/scoopi/output
     * @param printPayload
     * @return
     */
    private String getJobFilePath(final PrintPayload printPayload) {
        String path =
                String.join("", fileDir, "/", dirTimestamp, "/", fileBaseName,
                        "-", String.valueOf(printPayload.getJobInfo().getId()),
                        ".", fileExtension);
        if (!Paths.get(path).isAbsolute() && StringUtils.isNotBlank(baseDir)) {
            path = String.join("/", baseDir, path);
        }
        return FilenameUtils.separatorsToSystem(path);
    }

    /**
     * Append object to appender queue.
     * @param printPayload
     *            object to append, not null
     * @throws InterruptedException
     *             if interrupted while queue put operation
     */
    @Override
    public void append(final PrintPayload printPayload)
            throws InterruptedException {
        Validate.notNull(printPayload, "printPayload must not be null");
        if (isInitialized()) {
            getQueue().put(printPayload);
        }
    }
}
