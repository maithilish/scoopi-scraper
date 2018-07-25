package org.codetab.scoopi.di;

import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.inject.assistedinject.Assisted;

public interface BasicFactory {

    Payload getPayload();

    JobInfo getJobInfo(@Assisted("id") long id,
            @Assisted("locator") String locator,
            @Assisted("group") String group, @Assisted("task") String task,
            @Assisted("dataDef") String dataDef);

    StepInfo getStepInfo(@Assisted("stepName") String stepName,
            @Assisted("previousStepName") String priviousStepName,
            @Assisted("nextStepName") String nextStepName,
            @Assisted("className") String className);

    Server getServer(@Assisted("port") int port);

    WebAppContext getWebAppContext();
}
