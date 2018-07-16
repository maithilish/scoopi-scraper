package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadSleep {

    static final Logger LOGGER = LoggerFactory.getLogger(ThreadSleep.class);

    public void sleep(final long milli) {
        try {
            TimeUnit.MILLISECONDS.sleep(milli);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }
    }
}
