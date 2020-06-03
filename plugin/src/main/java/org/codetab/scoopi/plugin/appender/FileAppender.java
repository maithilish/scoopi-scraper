package org.codetab.scoopi.plugin.appender;

import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.utils.DateUtils;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.helper.IOHelper;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.PrintPayload;
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
            fileDir = FilenameUtils.getFullPath(filePath);
            fileBaseName = FilenameUtils.getBaseName(filePath);
            fileExtension = FilenameUtils.getExtension(filePath);
            dirTimestamp = DateUtils.formatDate(configs.getRunDateTime(),
                    configs.getConfig("outputDirTimestampPattern",
                            "ddMMMyyyy-HHmmss"));
            setInitialized(true);
        } catch (DefNotFoundException e) {
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
        int jobCount = 0;
        Random random = new Random();

        for (;;) {
            PrintPayload printPayload = null;
            try {
                printPayload = getQueue().take();
                if (printPayload.getData() == Marker.END_OF_STREAM) {
                    break;
                }
            } catch (final InterruptedException e) {
                final String message = spaceit("appender:", getName());
                errorLogger.log(CAT.INTERNAL, message, e);
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
                    final String message = spaceit("appender:", getName(),
                            " file path:", jobFilePath);
                    errorLogger.log(CAT.ERROR, message, e);
                }
                printPayload.finished();
            }
        }
        LOGGER.info("appender: {}, {} item appended", getName(), count);
    }

    private String getJobFilePath(final PrintPayload printPayload) {
        return FilenameUtils.separatorsToSystem(
                String.join("", fileDir, "/", dirTimestamp, "/", fileBaseName,
                        "-", String.valueOf(printPayload.getJobInfo().getId()),
                        ".", fileExtension));
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
