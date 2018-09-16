package org.codetab.scoopi.system;

import static java.util.Objects.nonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Log.CAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.codahale.metrics.Counter;

@Singleton
public class ErrorLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ErrorLogger.class);

    @Inject
    private MetricsHelper metricsHelper;

    private Counter errorCounter;

    public void start() {
        errorCounter = metricsHelper.getCounter(this, "system", "error");
    }

    public void log(final CAT type, final String message) {
        LOGGER.error("{} {}", type, message);
        LOGGER.debug("{} {}", type, message);
        errorCounter.inc();
    }

    public void log(final CAT type, final String message,
            final Throwable throwable) {

        LOGGER.error("type: {} message: {}", type, message);

        String exMessage = throwable.getMessage();
        String exName = throwable.getClass().getSimpleName();
        LOGGER.error("exception: {} : {}", exName, exMessage);
        Throwable cause = throwable.getCause();
        if (nonNull(cause)) {
            exName = cause.getClass().getSimpleName();
            exMessage = cause.getMessage();
            LOGGER.error("cause: {} : {}", exName, exMessage);
        }
        LOGGER.debug("{} {}", type, message, throwable);
        errorCounter.inc();
    }

    public void log(final Marker marker, final CAT type, final String message,
            final Throwable throwable) {

        LOGGER.error("type: {} message: {}", type, message);

        String exMessage = throwable.getMessage();
        String exName = throwable.getClass().getSimpleName();
        LOGGER.error(marker, "exception: {} : {}", exName, exMessage);
        Throwable cause = throwable.getCause();
        if (nonNull(cause)) {
            exName = cause.getClass().getSimpleName();
            exMessage = cause.getMessage();
            LOGGER.error(marker, "cause: {} : {}", exName, exMessage);
        }
        LOGGER.debug(marker, "{} {}", type, message, throwable);
        errorCounter.inc();
    }

    public long getErrorCount() {
        return errorCounter.getCount();
    }
}
