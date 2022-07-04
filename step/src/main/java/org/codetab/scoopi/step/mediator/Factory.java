package org.codetab.scoopi.step.mediator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Factory {

    public ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
