package org.codetab.scoopi.plugin.appender;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.model.Log.CAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Appends to a list
 * @author Maithilish
 *
 */
public final class ListAppender extends Appender {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ListAppender.class);

    private List<Object> list = new ArrayList<>();

    @Inject
    private ListAppender() {
    }

    @Override
    public void init() {
        setInitialized(true);
        LOGGER.info("created {}, name: {}", this.getClass().getSimpleName(),
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
            Object item = null;
            try {
                item = getQueue().take();
                count++;
                if (item == Marker.EOF) {
                    break;
                }
                list.add(item);
            } catch (InterruptedException e) {
                String message = String.join(" ", "appender:", getName());
                errorLogger.log(CAT.INTERNAL, message, e);
            }
        }
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
        notNull(object, "object must not be null");
        if (isInitialized()) {
            getQueue().put(object);
        }
    }

    public List<Object> getList() {
        return list;
    }
}
