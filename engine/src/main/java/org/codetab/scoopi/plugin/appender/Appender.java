package org.codetab.scoopi.plugin.appender;

import static org.apache.commons.lang3.Validate.notNull;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.codetab.scoopi.config.ConfigService;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract Appender Task.
 * @author Maithilish
 *
 */
public abstract class Appender implements Runnable {

    /**
     * <p>
     * Marker constants for appenders.
     * @author Maithilish
     *
     */
    public enum Marker {
        /**
         * End of input.
         */
        EOF
    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Appender.class);

    @Inject
    private ConfigService configService;
    @Inject
    private IPluginDef pluginDef;
    @Inject
    protected ErrorLogger errorLogger;

    /**
     * Queue to hold objects pushed to appenders.
     */
    private BlockingQueue<Object> queue;
    private String name;
    private boolean initialized = false;
    private Plugin plugin;

    /**
     * <p>
     * Abstract append method.
     * @param object
     *            object to append
     * @throws InterruptedException
     *             on interruption.
     */
    public abstract void append(Object object) throws InterruptedException;

    public abstract void init();

    /**
     * <p>
     * Initialises the blocking queue which is used to hold the objected pushed
     * to appender. By default, queue size is 4096 and it is configurable
     * globally with scoopi.appender.queuesize config. It is also possible to
     * override global size and configure size for an appender by adding
     * queuesize field to appender definition.
     * <p>
     * When large number of objects are appended to queue with inadequate
     * capacity then application may hang.
     * <p>
     * If size is invalid, then queue is not initialized.
     */
    public void initializeQueue() {
        String queueSize = null;
        try {
            queueSize = configService.getConfig("scoopi.appender.queueSize"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
        }
        try {
            queueSize = pluginDef.getValue(plugin, "queueSize");
        } catch (DefNotFoundException e) {
        }
        /*
         * default queue size. configService mock returns null so default is set
         * here
         */
        if (StringUtils.isBlank(queueSize)) {
            queueSize = "4096"; //$NON-NLS-1$
        }
        try {
            queue = new ArrayBlockingQueue<>(Integer.parseInt(queueSize));
            LOGGER.info("initialized appender: {}, queue size: {}", name,
                    queueSize);
        } catch (NumberFormatException e) {
            String message = spaceit("unable to create appender:", name);
            errorLogger.log(CAT.ERROR, message, e);
        }
    }

    public BlockingQueue<Object> getQueue() {
        return queue;
    }

    public String getName() {
        return name;
    }

    public void setName(final String appenderName) {
        notNull(appenderName, "appenderName must not be null");
        this.name = appenderName;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(final boolean initialized) {
        this.initialized = initialized;
    }

    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }

    public String getPluginField(final String field)
            throws DefNotFoundException {
        return pluginDef.getValue(plugin, field);
    }
}
