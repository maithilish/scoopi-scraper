package org.codetab.scoopi.engine.module;

import javax.inject.Inject;

import org.codetab.scoopi.plugin.appender.AppenderMediator;
import org.codetab.scoopi.step.mediator.JobMediator;
import org.codetab.scoopi.step.mediator.TaskMediator;

public class MediatorModule {

    @Inject
    private TaskMediator taskMediator;
    @Inject
    private JobMediator jobMediator;
    @Inject
    private AppenderMediator appenderMediator;

    public void initJobMediator() {
        jobMediator.init();
    }

    public void startJobMediator() {
        jobMediator.start();
    }

    public void startTaskMediator() {
        taskMediator.start();
    }

    public void waitForJobMediator() {
        jobMediator.waitForFinish();
    }

    public void waitForAppenderMediator() {
        appenderMediator.closeAll();
        appenderMediator.waitForFinish();
    }
}
