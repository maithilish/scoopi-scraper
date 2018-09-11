package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.system.ErrorLogger;

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
