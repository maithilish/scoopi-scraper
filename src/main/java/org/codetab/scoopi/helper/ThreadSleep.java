package org.codetab.scoopi.helper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.shared.StatService;

public class ThreadSleep {

    @Inject
    private StatService statService;

    public void sleep(final long milli) {
        try {
            TimeUnit.MILLISECONDS.sleep(milli);
        } catch (InterruptedException e) {
            statService.log(CAT.INTERNAL, e.getLocalizedMessage());
        }
    }
}
