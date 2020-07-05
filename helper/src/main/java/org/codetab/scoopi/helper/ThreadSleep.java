package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codetab.scoopi.metrics.Errors;
import org.codetab.scoopi.model.ERRORCAT;

// FIXME - interruptedFix remove this class

@Singleton
public class ThreadSleep {

    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private Errors errors;

    public void sleep(final long milli) {
        try {
            TimeUnit.MILLISECONDS.sleep(milli);
        } catch (InterruptedException e) {
            errors.inc();
            LOG.error("interrupted", ERRORCAT.INTERNAL, e);
        }
    }

    public void sleep(final long duration, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(duration);
        } catch (InterruptedException e) {
            errors.inc();
            LOG.error("interrupted", ERRORCAT.INTERNAL, e);
        }
    }
}
