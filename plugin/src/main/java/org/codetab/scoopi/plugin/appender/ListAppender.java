package org.codetab.scoopi.plugin.appender;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.PrintPayload;

/**
 * <p>
 * Appends to a list
 * @author Maithilish
 *
 */
public final class ListAppender extends Appender {

    private static final Logger LOG = LogManager.getLogger();

    private List<Object> list = new ArrayList<>();

    @Inject
    private ListAppender() {
    }

    @Override
    public void init() {
        setInitialized(true);
        LOG.info("created {}, name: {}", this.getClass().getSimpleName(),
                getName());
    }

    /**
     * Creates a file (PrintWriter) from appenders file field. Write the objects
     * taken from blocking queue until object is Marker.EOF.
     */
    @Override
    public void run() {
        int count = 0;
        for (;;) {
            PrintPayload printPayload = null;
            try {
                printPayload = getQueue().take();
                count++;
                if (printPayload.getData() == Marker.END_OF_STREAM) {
                    break;
                }
                list.add(printPayload.getData());
            } catch (InterruptedException e) {
                errors.inc();
                LOG.error("appender: {} [{}]", getName(), ERROR.INTERNAL, e);
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("appender: {}, {} item appended", getName(), count - 1);
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
        notNull(printPayload, "object must not be null");
        if (isInitialized()) {
            getQueue().put(printPayload);
        }
    }

    public List<Object> getList() {
        return list;
    }
}
