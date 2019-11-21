package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;

public class ThreadSleep {

    @Inject
    private ErrorLogger errorLogger;

    public void sleep(final long milli) {
        try {
            TimeUnit.MILLISECONDS.sleep(milli);
        } catch (InterruptedException e) {
            errorLogger.log(CAT.INTERNAL, e.getMessage());
        }
    }
}
