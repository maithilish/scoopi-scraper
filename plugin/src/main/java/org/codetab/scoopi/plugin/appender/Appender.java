package org.codetab.scoopi.plugin.appender;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.config.Configs;
import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERROR;
import org.codetab.scoopi.model.Plugin;
import org.codetab.scoopi.model.PrintPayload;

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
        END_OF_STREAM
    }

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Configs configs;
    @Inject
    private IPluginDef pluginDef;
    @Inject
    protected Errors errors;

    /**
     * Queue to hold objects pushed to appenders.
     */
    private BlockingQueue<PrintPayload> queue;
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
    public abstract void append(PrintPayload printPayload)
            throws InterruptedException;

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
            queueSize = configs.getConfig("scoopi.appender.queueSize"); //$NON-NLS-1$
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
            LOG.info("initialized appender: {}, queue size: {}", name,
                    queueSize);
        } catch (NumberFormatException e) {
            // FIXME - logfix, throw critical exception
            errors.inc();
            LOG.error("unable to create appender: {} [{}]", name,
                    ERROR.INTERNAL);
        }
    }

    public BlockingQueue<PrintPayload> getQueue() {
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
